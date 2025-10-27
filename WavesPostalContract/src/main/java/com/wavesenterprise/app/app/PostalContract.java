package com.wavesenterprise.app.app;

import com.wavesenterprise.app.api.IPostalContract;
import com.wavesenterprise.app.domain.*;
import com.wavesenterprise.sdk.contract.api.state.ContractState;
import com.wavesenterprise.sdk.contract.core.annotation.ContractHandler;

import java.util.*;

/**
 * Полная реализация почтовой системы
 * Все функции по ТЗ, минимальный код
 */
@ContractHandler
public class PostalContract implements IPostalContract {

    private final ContractState state;
    private final String caller;

    public PostalContract(ContractState state, String caller) {
        this.state = state;
        this.caller = caller;
    }

    // === ИНИЦИАЛИЗАЦИЯ ===
    @Override
    public void init() {
        // Администратор
        User admin = new User("Семенов Семен Семенович", "Адрес админа", 50, Role.ADMIN, null);
        state.put("user_3NjCnTjxaoaJtGZXUvLcZPq9Lq4R1Lrg2JX", admin);

        // Сотрудник Ростов
        User employee1 = new User("Петров Петр Петрович", "Ростов-на-Дону", 50, Role.EMPLOYEE, "RR344000");
        state.put("user_3NnrwM9Gkqz3MavXKM5aNTX8ir9HhDYfLS1", employee1);

        // Сотрудник Таганрог
        User employee2 = new User("Антонов Антон Антонович", "Таганрог", 50, Role.EMPLOYEE, "RR347900");
        state.put("user_3Ns49LANhFK7ePAyzVJ5KpQMZWJHQnUn3oK", employee2);

        // Обычный пользователь
        User user = new User("Юрьев Юрий Юрьевич", "Адрес Юрия", 50, Role.USER, null);
        state.put("user_3NpubPefm9nRqXf5gXdsz3JaZUJYZ6ArvFA", user);

        state.put("postal_counter", 0L);
        state.put("transfer_counter", 0L);
    }

    // === РЕГИСТРАЦИЯ И УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯМИ ===
    @Override
    public void registerUser(String name, String homeAddress) {
        if (state.contains("user_" + caller)) {
            throw new RuntimeException("Пользователь уже зарегистрирован");
        }
        User newUser = new User(name, homeAddress, 50, Role.USER, null);
        state.put("user_" + caller, newUser);
    }

    @Override
    public void addEmployee(String userAddress, String postOfficeId) {
        checkAdmin();
        User user = getUser(userAddress);
        user.role = Role.EMPLOYEE;
        user.postOfficeId = postOfficeId;
        state.put("user_" + userAddress, user);
    }

    @Override
    public void removeEmployee(String userAddress) {
        checkAdmin();
        User user = getUser(userAddress);
        user.role = Role.USER;
        user.postOfficeId = null;
        state.put("user_" + userAddress, user);
    }

    @Override
    public void setEmployeePostOffice(String userAddress, String newPostOfficeId) {
        checkAdmin();
        User user = getUser(userAddress);
        if (user.role != Role.EMPLOYEE) throw new RuntimeException("Пользователь не сотрудник");
        user.postOfficeId = newPostOfficeId;
        state.put("user_" + userAddress, user);
    }

    // === ПОЧТОВЫЕ ОТПРАВЛЕНИЯ ===
    @Override
    public String createPostalItem(String receiver, PostalType type, int postalClass,
                                   double weight, double declaredValue, String destinationAddress, String sendingAddress) {
        // Проверка сотрудника
        User employee = getUser(caller);
        if (employee.role != Role.EMPLOYEE) throw new RuntimeException("Только сотрудники могут создавать отправления");

        // Проверка веса
        if (weight > 10.0) throw new RuntimeException("Вес не может превышать 10 кг");

        // Класс по умолчанию
        if (postalClass < 1 || postalClass > 3) postalClass = 3;

        // Расчет стоимости
        double costPerKg = getCostByClass(postalClass);
        int days = getDaysByClass(postalClass);
        double totalCost = costPerKg * weight + declaredValue * 0.1;

        // Проверка баланса
        if (employee.balance < totalCost) throw new RuntimeException("Недостаточно средств");

        // Создание отправления
        PostalItem item = new PostalItem();
        item.trackId = generateTrackId(employee.postOfficeId);
        item.sender = caller;
        item.receiver = receiver;
        item.type = type;
        item.postalClass = postalClass;
        item.deliveryDays = days;
        item.deliveryCost = costPerKg;
        item.weight = weight;
        item.declaredValue = declaredValue;
        item.totalCost = totalCost;
        item.destinationAddress = destinationAddress;
        item.sendingAddress = sendingAddress;
        item.status = "CREATED";
        item.createdAt = System.currentTimeMillis();

        // Списание средств и сохранение
        employee.balance -= (long) totalCost;
        state.put("user_" + caller, employee);
        state.put("postal_" + item.trackId, item);

        return item.trackId;
    }

    @Override
    public void receivePostalItem(String trackId) {
        PostalItem item = getPostalItem(trackId);
        if (!item.receiver.equals(caller)) throw new RuntimeException("Только получатель может получить отправление");
        item.status = "DELIVERED";
        state.put("postal_" + trackId, item);
    }

    @Override
    public void rejectPostalItem(String trackId) {
        PostalItem item = getPostalItem(trackId);
        if (!item.receiver.equals(caller)) throw new RuntimeException("Только получатель может отказаться");
        item.status = "REJECTED";
        state.put("postal_" + trackId, item);
    }

    // === ДЕНЕЖНЫЕ ПЕРЕВОДЫ ===
    @Override
    public String sendMoneyTransfer(String receiver, long amount, long lifetimeDays) {
        User sender = getUser(caller);
        if (sender.balance < amount) throw new RuntimeException("Недостаточно средств");

        sender.balance -= amount;
        state.put("user_" + caller, sender);

        long transferId = state.get("transfer_counter") + 1;
        state.put("transfer_counter", transferId);

        MoneyTransfer transfer = new MoneyTransfer(caller, receiver, amount, lifetimeDays);
        state.put("transfer_" + transferId, transfer);

        return String.valueOf(transferId);
    }

    @Override
    public void acceptMoneyTransfer(String transferId) {
        MoneyTransfer transfer = getTransfer(transferId);
        if (!transfer.receiver.equals(caller)) throw new RuntimeException("Только получатель может принять перевод");

        User receiver = getUser(caller);
        receiver.balance += transfer.amount;
        state.put("user_" + caller, receiver);

        transfer.status = "ACCEPTED";
        state.put("transfer_" + transferId, transfer);
    }

    @Override
    public void rejectMoneyTransfer(String transferId) {
        MoneyTransfer transfer = getTransfer(transferId);
        if (!transfer.receiver.equals(caller)) throw new RuntimeException("Только получатель может отклонить перевод");

        User sender = getUser(transfer.sender);
        sender.balance += transfer.amount;
        state.put("user_" + transfer.sender, sender);

        transfer.status = "REJECTED";
        state.put("transfer_" + transferId, transfer);
    }

    @Override
    public void cancelMoneyTransfer(String transferId) {
        MoneyTransfer transfer = getTransfer(transferId);
        if (!transfer.sender.equals(caller)) throw new RuntimeException("Только отправитель может отменить перевод");
        if ("ACCEPTED".equals(transfer.status)) throw new RuntimeException("Нельзя отменить принятый перевод");

        User sender = getUser(caller);
        sender.balance += transfer.amount;
        state.put("user_" + caller, sender);

        transfer.status = "CANCELLED";
        state.put("transfer_" + transferId, transfer);
    }

    // === ЛИЧНЫЙ КАБИНЕТ ===
    @Override
    public void updatePersonalInfo(String name, String homeAddress) {
        User user = getUser(caller);
        user.name = name;
        user.homeAddress = homeAddress;
        state.put("user_" + caller, user);
    }

    @Override
    public Object getPostalHistory() {
        List<String> result = new ArrayList<>();
        for (String key : state.keys()) {
            if (key.startsWith("postal_")) {
                PostalItem item = state.get(key);
                if (item.sender.equals(caller) || item.receiver.equals(caller)) {
                    result.add(item.trackId + " | " + item.status + " | " + item.destinationAddress);
                }
            }
        }
        return result;
    }

    @Override
    public Object trackPostalItem(String trackId) {
        PostalItem item = getPostalItem(trackId);
        return "Трек: " + item.trackId +
                "\nСтатус: " + item.status +
                "\nАдрес: " + item.destinationAddress +
                "\nТип: " + item.type;
    }

    @Override
    public Object getActivePostalItems() {
        List<String> result = new ArrayList<>();
        for (String key : state.keys()) {
            if (key.startsWith("postal_")) {
                PostalItem item = state.get(key);
                if ((item.sender.equals(caller) || item.receiver.equals(caller)) &&
                        !"DELIVERED".equals(item.status) && !"REJECTED".equals(item.status)) {
                    result.add(item.trackId + " - " + item.status);
                }
            }
        }
        return result;
    }

    @Override
    public Object getAllPostals() {
        checkAdmin();
        List<String> result = new ArrayList<>();
        for (String key : state.keys()) {
            if (key.startsWith("postal_")) {
                PostalItem item = state.get(key);
                result.add(item.trackId + " | " + item.sender + " -> " + item.receiver + " | " + item.status);
            }
        }
        return result;
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===
    private User getUser(String address) {
        User user = state.get("user_" + address);
        if (user == null) throw new RuntimeException("Пользователь не найден: " + address);
        return user;
    }

    private PostalItem getPostalItem(String trackId) {
        PostalItem item = state.get("postal_" + trackId);
        if (item == null) throw new RuntimeException("Отправление не найдено: " + trackId);
        return item;
    }

    private MoneyTransfer getTransfer(String transferId) {
        MoneyTransfer transfer = state.get("transfer_" + transferId);
        if (transfer == null) throw new RuntimeException("Перевод не найден: " + transferId);
        return transfer;
    }

    private void checkAdmin() {
        User user = getUser(caller);
        if (user.role != Role.ADMIN) throw new RuntimeException("Только администратор может выполнить это действие");
    }

    private String generateTrackId(String postOfficeId) {
        long counter = state.get("postal_counter") + 1;
        state.put("postal_counter", counter);

        Calendar cal = Calendar.getInstance();
        String date = String.format("%02d%02d%04d",
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.YEAR));

        return "RR" + date + String.format("%03d", counter) + postOfficeId + "344000";
    }

    private double getCostByClass(int postalClass) {
        if (postalClass == 1) return 0.5;
        if (postalClass == 2) return 0.3;
        return 0.1;
    }

    private int getDaysByClass(int postalClass) {
        if (postalClass == 1) return 5;
        if (postalClass == 2) return 10;
        return 15;
    }
}