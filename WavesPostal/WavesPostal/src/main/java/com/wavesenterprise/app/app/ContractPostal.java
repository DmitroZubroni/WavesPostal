package com.wavesenterprise.app.app;

import com.wavesenterprise.app.api.IContract;
import com.wavesenterprise.app.domain.Transfer;
import com.wavesenterprise.app.domain.Postal;
import com.wavesenterprise.app.domain.User;
import com.wavesenterprise.app.domain.Role;

import com.wavesenterprise.sdk.contract.api.state.ContractState;
import java.util.*;

public record ContractPostal(ContractState state) implements IContract {

    private static final String USERS_KEY = "users";
    private static final String POSTALS_KEY = "postals";
    private static final String TRANSFERS_KEY = "transfers";
    private static final String COUNTER_KEY = "counter";

    /** Инициализация контракта */
    @Override
    public void init() {
        Map<String, User> users = new HashMap<>();
        users.put("admin", new User("admin", "Админ", Role.ADMINISTRATOR, 1000));
        state.put(USERS_KEY, users);
        state.put(COUNTER_KEY, 0);
    }

    /** Проверка прав администратора */
    private void checkAdmin() {
        User user = getUsers().get(getCaller());
        if (user == null || user.role != Role.ADMINISTRATOR)
            throw new RuntimeException("Требуются права администратора");
    }

    /** Проверка прав сотрудника */
    private void checkEmployee() {
        User user = getUsers().get(getCaller());
        if (user == null || user.role != Role.EMPLOYEE)
            throw new RuntimeException("Требуются права сотрудника почтового отделения");
    }

    /** Генерация трек-номера */
    private String generateTrackNumber(String sendIndex, String destIndex) {
        int counter = state.get(COUNTER_KEY, Integer.class) + 1;
        state.put(COUNTER_KEY, counter);
        String date = "25102025"; // Фиксированная дата для примера
        return "RR" + date + String.format("%04d", counter) + sendIndex + destIndex;
    }

    /** Добавление сотрудника (только админ) */
    @Override
    public void addEmployee(String userAddress, String postOfficeId) {
        checkAdmin();
        Map<String, User> users = getUsers();
        User user = users.get(userAddress);
        if (user == null) throw new RuntimeException("Пользователь не найден");
        user.role = Role.EMPLOYEE;
        user.postOfficeId = postOfficeId;
        users.put(userAddress, user);
        state.put(USERS_KEY, users);
    }

    /** Удаление сотрудника (только админ) */
    @Override
    public void removeEmployee(String userAddress) {
        checkAdmin();
        Map<String, User> users = getUsers();
        User user = users.get(userAddress);
        if (user == null || user.role != Role.EMPLOYEE) throw new RuntimeException("Пользователь не сотрудник");
        user.role = Role.USER;
        user.postOfficeId = null;
        users.put(userAddress, user);
        state.put(USERS_KEY, users);
    }

    /** Изменение ID почтового отделения */
    @Override
    public void setIdPostal(String userAddress, String newPostOfficeId) {
        checkAdmin();
        Map<String, User> users = getUsers();
        User u = users.get(userAddress);
        if (u == null || u.role != Role.EMPLOYEE) throw new RuntimeException("Пользователь не сотрудник");
        u.postOfficeId = newPostOfficeId;
        users.put(userAddress, u);
        state.put(USERS_KEY, users);
    }

    /** Регистрация пользователя */
    @Override
    public void registration(String name, String homeAddress) {
        String caller = getCaller();
        Map<String, User> users = getUsers();
        if (users.containsKey(caller)) throw new RuntimeException("Уже зарегистрирован");
        users.put(caller, new User(caller, name, Role.USER, 100));
        state.put(USERS_KEY, users);
    }

    /** Добавление почтового отправления (только сотрудник) */
    @Override
    public void addPostal(String sender, String receiver, String type, int postalClass,
                          double weight, double declaredValue, String destinationAddress, String sendingAddress) {
        checkEmployee();
        Map<String, User> users = getUsers();
        User senderUser = users.get(sender);

        if (senderUser == null) throw new RuntimeException("Отправитель не найден");


        double cost = weight*10 + declaredValue*0.1;
        if (senderUser.balance < cost) throw new RuntimeException("Недостаточно средств");

        String track = generateTrackNumber(sendingAddress, destinationAddress);
        Postal p = new Postal(track, sender, receiver, weight, declaredValue, cost, sendingAddress, destinationAddress);

        senderUser.balance -= cost;
        users.put(sender, senderUser);
        Map<String, Postal> postals = getPostals();
        postals.put(track, p);

        state.put(USERS_KEY, users);
        state.put(POSTALS_KEY, postals);
    }

    /** Отправка почтового отправления пользователем */
    @Override
    public void sendPostal(String receiver, String type, int postalClass,
                           double weight, double declaredValue, String destinationAddress) {
        addPostal(getCaller(), receiver, type, postalClass, weight, declaredValue, destinationAddress, "0001");
    }

    /** Получение почтового отправления */
    @Override
    public void getPostal(String trackNumber) {
        Map<String, Postal> postals = getPostals();
        Postal p = postals.get(trackNumber);
        if (p == null) throw new RuntimeException("Отправление не найдено");
        if (!p.recipient.equals(getCaller())) throw new RuntimeException("Не ваше отправление");
        p.status = "DELIVERED";
        postals.put(trackNumber, p);
        state.put(POSTALS_KEY, postals);
    }

    /** Отказ от почтового отправления */
    @Override
    public void refusePostal(String trackNumber) {
        Map<String, Postal> postals = getPostals();
        Postal p = postals.get(trackNumber);
        if (p == null) throw new RuntimeException("Отправление не найдено");
        if (!p.recipient.equals(getCaller())) throw new RuntimeException("Не ваше отправление");
        p.status = "REFUSED";
        postals.put(trackNumber, p);
        state.put(POSTALS_KEY, postals);
    }

    /** Отправка денежного перевода */
    @Override
    public void sendTransfer(String receiver, double amount, int lifetimeDays) {
        Map<String, User> users = getUsers();
        User sender = users.get(getCaller());
        if (sender.balance < amount) throw new RuntimeException("Недостаточно средств");
        sender.balance -= amount;
        users.put(getCaller(), sender);

        Transfer t = new Transfer("T"+System.currentTimeMillis(), getCaller(), receiver, amount);
        Map<String, Transfer> transfers = getTransfers();
        transfers.put(t.id, t);

        state.put(USERS_KEY, users);
        state.put(TRANSFERS_KEY, transfers);
    }

    /** Получение денежного перевода */
    @Override
    public void getTransfer(String transferId) {
        Map<String, Transfer> transfers = getTransfers();
        Transfer t = transfers.get(transferId);
        if (t == null) throw new RuntimeException("Перевод не найден");
        if (!t.recipient.equals(getCaller())) throw new RuntimeException("Не ваш перевод");

        Map<String, User> users = getUsers();
        User rec = users.get(getCaller());
        rec.balance += t.amount;
        t.status = "ACCEPTED";

        transfers.put(transferId, t);
        state.put(USERS_KEY, users);
        state.put(TRANSFERS_KEY, transfers);
    }

    /** Отказ от денежного перевода */
    @Override
    public void refuseTransfer(String transferId) {
        Map<String, Transfer> transfers = getTransfers();
        Transfer t = transfers.get(transferId);
        if (t == null) throw new RuntimeException("Перевод не найден");
        if (!t.recipient.equals(getCaller())) throw new RuntimeException("Не ваш перевод");

        Map<String, User> users = getUsers();
        User sender = users.get(t.sender);
        sender.balance += t.amount;
        t.status = "REFUSED";

        transfers.put(transferId, t);
        state.put(USERS_KEY, users);
        state.put(TRANSFERS_KEY, transfers);
    }



    /** Отследить отправление */
    @Override
    public Postal trackDeparture(String trackNumber) {
        Postal p = getPostals().get(trackNumber);
        if (p == null) throw new RuntimeException("Отправление не найдено");
        String caller = getCaller();
        if (!caller.equals(p.sender) && !caller.equals(p.recipient))
            throw new RuntimeException("Нет прав для просмотра отправления");
        return p;
    }

    /** Генерация трек-номера */
    @Override
    public String generationTrackId(String sendingIndex, String destinationIndex) {
        return generateTrackNumber(sendingIndex, destinationIndex);
    }

    /** Активные отправления */
    @Override
    public List<Postal> getActiveDeparture() {
        List<Postal> result = new ArrayList<>();
        String caller = getCaller();
        for (Postal p : getPostals().values()) {
            if ((caller.equals(p.sender) || caller.equals(p.recipient)) && !"DELIVERED".equals(p.status))
                result.add(p);
        }
        return result;
    }

    /** История отправлений */
    @Override
    public List<Postal> getHistoryDeparture() {
        List<Postal> result = new ArrayList<>();
        String caller = getCaller();
        for (Postal p : getPostals().values()) {
            if (caller.equals(p.sender) || caller.equals(p.recipient))
                result.add(p);
        }
        return result;
    }

    /** Информация о пользователе */
    @Override
    public User getUserInfo(String address) {
        return getUsers().get(address);
    }

    /** Изменение персональных данных */
    @Override
    public void setPersonInfo(String name, String homeAddress) {
        Map<String, User> users = getUsers();
        User u = users.get(getCaller());
        u.name = name;
        state.put(USERS_KEY, users);
    }

    /** Получение адреса вызывающего (имитация getCaller) */
    private String getCaller() {
        return "caller_address"; // Заглушка
    }

    /** Вспомогательные методы для работы со state */
    @SuppressWarnings("unchecked")
    private Map<String, User> getUsers() {
        Map<String, User> users = state.get(USERS_KEY, Map.class);
        return users != null ? users : new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Postal> getPostals() {
        Map<String, Postal> postals = state.get(POSTALS_KEY, Map.class);
        return postals != null ? postals : new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Transfer> getTransfers() {
        Map<String, Transfer> transfers = state.get(TRANSFERS_KEY, Map.class);
        return transfers != null ? transfers : new HashMap<>();
    }
}