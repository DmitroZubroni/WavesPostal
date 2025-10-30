package com.wavesenterprise.app.app;

import com.wavesenterprise.app.api.IPostalContract;
import com.wavesenterprise.app.domain.*;
import com.wavesenterprise.sdk.contract.api.annotation.ContractHandler;
import com.wavesenterprise.sdk.contract.api.domain.ContractCall;
import com.wavesenterprise.sdk.contract.api.state.ContractState;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.wavesenterprise.app.api.IPostalContract.Keys.*;
import static com.wavesenterprise.app.api.IPostalContract.ParcelStatus.*;
import static com.wavesenterprise.app.api.IPostalContract.Role.*;

@ContractHandler
public class PostContract implements IPostalContract {

    private final ContractState state;
    private final ContractCall call;

    public PostContract(ContractState state, ContractCall call) {
        this.state = state;
        this.call = call;
    }

    @Override
    public void init() {
        // Инициализация контракта - создаем администратора и начальные данные
        String callerAddress = call.getCaller();
        state.put(ADMIN_ADDRESS, callerAddress);
        state.put(PARCEL_COUNTER, 0);
        state.put(TRANSFER_COUNTER, 0);

        // Создаем администратора системы
        User admin = new User(
                "Семенов Семен Семенович",
                "г. Ростов-на-Дону, ул. Административная, 1",
                callerAddress,
                50.0,
                ADMIN
        );
        state.put(USERS_PREFIX + callerAddress, admin);

        createInitialUsers();
        createPostOffices();
    }

    private void createInitialUsers() {
        // Создаем начальных пользователей системы
        User employee1 = new User(
                "Петров Петр Петрович",
                "г. Ростов-на-Дону, ул. Центральная, 10",
                "3NnrwM9Gkqz3MavXKM5aNTX8ir9HhDYfLS1",
                50.0,
                EMPLOYEE
        );
        employee1.setPostId("344000");
        state.put(USERS_PREFIX + employee1.getBlockchainAddress(), employee1);

        User employee2 = new User(
                "Антонов Антон Антонович",
                "г. Таганрог, ул. Главная, 5",
                "3Ns49LANhFK7ePAyzVJ5KpQMZWJHQnUn3oK",
                50.0,
                EMPLOYEE
        );
        employee2.setPostId("347900");
        state.put(USERS_PREFIX + employee2.getBlockchainAddress(), employee2);

        User user = new User(
                "Юрьев Юрий Юрьевич",
                "г. Батайск, ул. Пользовательская, 3",
                "3NpubPefm9nRqXf5gXdsz3JaZUJYZ6ArvFA",
                50.0,
                USER
        );
        state.put(USERS_PREFIX + user.getBlockchainAddress(), user);
    }

    private void createPostOffices() {
        // Создаем почтовые отделения Ростовской области
        int[][] offices = {
                {344000}, // Ростов-на-Дону
                {347900, 347901, 347902, 347903}, // Таганрог
                {346770, 346771, 346772, 346773}, // Батайск
                {343760, 343761, 343762, 343763}, // Волгодонск
                {346780, 346781, 346782, 346783}  // Азов
        };

        for (int[] cityOffices : offices) {
            for (int officeId : cityOffices) {
                String officeType = officeId % 1000 == 0 ? "MAIN_OFFICE" : "REGULAR_OFFICE";
                PostOffice office = new PostOffice(officeId, officeType);
                state.put(OFFICES_PREFIX + officeId, office);
            }
        }
    }

    @Override
    public void registerUser(String name, String homeAddress) {
        // Регистрация нового пользователя в системе
        String callerAddress = call.getCaller();

        if (state.get(USERS_PREFIX + callerAddress, User.class) != null) {
            throw new RuntimeException("Пользователь уже зарегистрирован в системе");
        }

        User newUser = new User(name, homeAddress, callerAddress, 50.0, USER);
        state.put(USERS_PREFIX + callerAddress, newUser);
    }

    @Override
    public void manageEmployee(String employeeAddress, int postOfficeId, boolean isAdd) {
        // Администратор добавляет/удаляет сотрудников почтовых отделений
        checkAdminRights();

        User employee = state.get(USERS_PREFIX + employeeAddress, User.class);
        if (employee == null) {
            throw new RuntimeException("Пользователь не найден");
        }

        if (isAdd) {
            employee.setRole(EMPLOYEE);
            employee.setPostId(String.valueOf(postOfficeId));
        } else {
            employee.setRole(USER);
            employee.setPostId(null);
        }

        state.put(USERS_PREFIX + employeeAddress, employee);
    }

    @Override
    public void sendPostal(String toAddress, String type, String shippingClass,
                           double weight, double declaredValue, String addressTo,
                           int postOfficeFrom, int postOfficeTo) {
        // Отправка почтового отправления

        if (weight > 10.0) {
            throw new RuntimeException("Вес отправления не может превышать 10 кг");
        }

        String callerAddress = call.getCaller();
        User sender = state.get(USERS_PREFIX + callerAddress, User.class);
        User receiver = state.get(USERS_PREFIX + toAddress, User.class);

        if (sender == null || receiver == null) {
            throw new RuntimeException("Отправитель или получатель не найден");
        }

        // Генерация трек-номера и создание отправления
        String trackNumber = generateTrackNumber(postOfficeFrom, postOfficeTo);
        Postalitem postal = new Postalitem(
                trackNumber, callerAddress, toAddress, type, shippingClass,
                weight, declaredValue, addressTo, postOfficeFrom, postOfficeTo
        );

        // Расчет и проверка стоимости
        double totalCost = calculateTotalCost(postal);
        if (sender.getBalance() < totalCost) {
            throw new RuntimeException("Недостаточно средств на счете для отправки");
        }

        // Списание средств и сохранение отправления
        sender.setBalance(sender.getBalance() - totalCost);
        int parcelId = state.get(PARCEL_COUNTER, Integer.class) + 1;
        state.put(PARCELS_PREFIX + parcelId, postal);
        state.put(PARCEL_COUNTER, parcelId);
        state.put(USERS_PREFIX + callerAddress, sender);

        // Фиксация начальной точки транзита
        addTransitPoint(postal, callerAddress, postOfficeFrom, "Принято в отделении отправления");
    }

    @Override
    public void processParcel(String parcelId, int nextPostOfficeId) {
        // Обработка отправления сотрудником почты (перемещение между отделениями)
        String callerAddress = call.getCaller();
        User employee = state.get(USERS_PREFIX + callerAddress, User.class);

        if (!EMPLOYEE.equals(employee.getRole())) {
            throw new RuntimeException("Только сотрудники почты могут обрабатывать отправления");
        }

        Postalitem postal = state.get(PARCELS_PREFIX + parcelId, Postalitem.class);
        if (postal == null) {
            throw new RuntimeException("Отправление не найдено");
        }

        // Обновление местоположения и статуса
        postal.setCurrentOffice(nextPostOfficeId);
        postal.setStatus(IN_TRANSIT);

        // Если прибыло в конечное отделение - отмечаем как доставленное
        if (nextPostOfficeId == postal.getPostOfficeTo()) {
            postal.setStatus(DELIVERED);
        }

        state.put(PARCELS_PREFIX + parcelId, postal);
        addTransitPoint(postal, callerAddress, nextPostOfficeId, "Перемещено в следующее отделение");
    }

    @Override
    public void receiveParcel(String parcelId) {
        // Получение отправления адресатом
        String callerAddress = call.getCaller();
        Postalitem postal = state.get(PARCELS_PREFIX + parcelId, Postalitem.class);

        if (postal == null || !callerAddress.equals(postal.getTo())) {
            throw new RuntimeException("Отправление не найдено или вы не являетесь получателем");
        }

        if (!DELIVERED.equals(postal.getStatus())) {
            throw new RuntimeException("Отправление еще не доставлено в ваше отделение");
        }

        postal.setStatus(RECEIVED);
        state.put(PARCELS_PREFIX + parcelId, postal);
        addTransitPoint(postal, callerAddress, postal.getPostOfficeTo(), "Получено адресатом");
    }

    @Override
    public void denyParcel(String parcelId) {
        // Отказ от получения отправления
        String callerAddress = call.getCaller();
        Postalitem postal = state.get(PARCELS_PREFIX + parcelId, Postalitem.class);

        if (postal == null || !callerAddress.equals(postal.getTo())) {
            throw new RuntimeException("Отправление не найдено или вы не являетесь получателем");
        }

        postal.setStatus(DENIED);
        state.put(PARCELS_PREFIX + parcelId, postal);
        addTransitPoint(postal, callerAddress, postal.getPostOfficeTo(), "Отказ от получения");
    }

    @Override
    public void sendMoneyTransfer(String toAddress, double amount, int lifeTimeDays) {
        // Отправка денежного перевода
        String callerAddress = call.getCaller();
        User sender = state.get(USERS_PREFIX + callerAddress, User.class);
        User receiver = state.get(USERS_PREFIX + toAddress, User.class);

        if (sender == null || receiver == null) {
            throw new RuntimeException("Отправитель или получатель не найден");
        }

        if (sender.getBalance() < amount) {
            throw new RuntimeException("Недостаточно средств для перевода");
        }

        // Создание и сохранение перевода
        MoneyTransfer transfer = new MoneyTransfer(callerAddress, toAddress, amount, lifeTimeDays);
        sender.setBalance(sender.getBalance() - amount);

        int transferId = state.get(TRANSFER_COUNTER, Integer.class) + 1;
        state.put(TRANSFERS_PREFIX + transferId, transfer);
        state.put(TRANSFER_COUNTER, transferId);
        state.put(USERS_PREFIX + callerAddress, sender);
    }

    @Override
    public void acceptMoneyTransfer(String transferId) {
        // Принятие денежного перевода получателем
        String callerAddress = call.getCaller();
        MoneyTransfer transfer = state.get(TRANSFERS_PREFIX + transferId, MoneyTransfer.class);

        if (transfer == null || !callerAddress.equals(transfer.getTo())) {
            throw new RuntimeException("Перевод не найден или вы не являетесь получателем");
        }

        if (!transfer.isActive()) {
            throw new RuntimeException("Перевод уже был обработан");
        }

        // Зачисление средств получателю
        User receiver = state.get(USERS_PREFIX + callerAddress, User.class);
        receiver.setBalance(receiver.getBalance() + transfer.getAmount());
        transfer.setActive(false);

        state.put(USERS_PREFIX + callerAddress, receiver);
        state.put(TRANSFERS_PREFIX + transferId, transfer);
    }

    @Override
    public void cancelMoneyTransfer(String transferId) {
        // Отмена денежного перевода отправителем
        String callerAddress = call.getCaller();
        MoneyTransfer transfer = state.get(TRANSFERS_PREFIX + transferId, MoneyTransfer.class);

        if (transfer == null || !callerAddress.equals(transfer.getFrom())) {
            throw new RuntimeException("Перевод не найден или вы не являетесь отправителем");
        }

        if (!transfer.isActive()) {
            throw new RuntimeException("Перевод уже принят получателем");
        }

        // Возврат средств отправителю
        User sender = state.get(USERS_PREFIX + callerAddress, User.class);
        sender.setBalance(sender.getBalance() + transfer.getAmount());
        transfer.setActive(false);

        state.put(USERS_PREFIX + callerAddress, sender);
        state.put(TRANSFERS_PREFIX + transferId, transfer);
    }

    @Override
    public void updatePersonalInfo(String name, String homeAddress) {
        // Обновление персональных данных пользователя
        String callerAddress = call.getCaller();
        User user = state.get(USERS_PREFIX + callerAddress, User.class);

        if (user == null) {
            throw new RuntimeException("Пользователь не найден");
        }

        user.setName(name);
        user.setHomeAddress(homeAddress);
        state.put(USERS_PREFIX + callerAddress, user);
    }

    @Override
    public String trackParcel(String trackNumber) {
        // Отслеживание отправления по трек-номеру
        int parcelCount = state.get(PARCEL_COUNTER, Integer.class);

        for (int i = 1; i <= parcelCount; i++) {
            Postalitem parcel = state.get(PARCELS_PREFIX + i, Postalitem.class);
            if (parcel != null && trackNumber.equals(parcel.getTrackNumber())) {
                return String.format("Статус: %s, Текущее отделение: %d",
                        parcel.getStatus(), parcel.getCurrentOffice());
            }
        }

        throw new RuntimeException("Отправление с указанным трек-номером не найдено");
    }

    private void checkAdminRights() {
        // Проверка прав администратора
        String adminAddress = state.get(ADMIN_ADDRESS, String.class);
        String callerAddress = call.getCaller();

        if (!callerAddress.equals(adminAddress)) {
            throw new RuntimeException("Только администратор может выполнять эту операцию");
        }
    }

    private String generateTrackNumber(int postOfficeFrom, int postOfficeTo) {
        // Генерация трек-номера по формату: RR + дата + порядковый номер + индексы
        String date = new SimpleDateFormat("ddMMyyyy").format(new Date());
        int dailyCounter = 25; // В реальной системе должен быть счетчик за день
        return String.format("RR%s%d%d%d", date, dailyCounter, postOfficeFrom, postOfficeTo);
    }

    private double calculateTotalCost(Postalitem postal) {
        // Расчет итоговой стоимости отправления
        double baseCost = getBaseCost(postal.getShippingClass());
        return baseCost * postal.getWeight() + postal.getDeclaredValue() * 0.1;
    }

    private double getBaseCost(String shippingClass) {
        // Получение базовой стоимости в зависимости от класса отправления
        return switch (shippingClass) {
            case "CLASS_1" -> 0.5;
            case "CLASS_2" -> 0.3;
            default -> 0.1; // CLASS_3 и по умолчанию
        };
    }

    private void addTransitPoint(Postalitem postal, String employeeAddress, int officeId, String action) {
        // Добавление точки транзита в историю отправления
        // В реальной системе здесь бы сохранялась информация о транзите
        new Transit(employeeAddress, postal.getTrackNumber(), postal.getWeight(), officeId, action);
        // Транзит создается, но не сохраняется в state для упрощения
    }
}