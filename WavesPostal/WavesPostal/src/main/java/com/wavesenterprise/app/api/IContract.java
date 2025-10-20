package com.wavesenterprise.app.api;
import com.wavesenterprise.app.domain.Postal;
import com.wavesenterprise.app.domain.User;
import com.wavesenterprise.sdk.contract.api.annotation.ContractAction;
import com.wavesenterprise.sdk.contract.api.annotation.ContractInit;

import java.util.List;

public interface IContract {

    /** Инициализация контракта (создание админа и стартовых пользователей) */
    @ContractInit
    void init();

    /** Регистрация нового пользователя */
    @ContractAction
    void registration(String name, String homeAddress);

    /** Добавление сотрудника (только админ) */
    @ContractAction
    void addEmployee(String userAddress, String postOfficeId);

    /** Удаление сотрудника (только админ) */
    @ContractAction
    void removeEmployee(String userAddress);

    /** Изменение ID почтового отделения сотрудника (только админ) */
    @ContractAction
    void setIdPostal(String userAddress, String newPostOfficeId);

    /** Добавление почтового отправления (только сотрудник) */
    @ContractAction
    void addPostal(String sender, String receiver, String type, int postalClass,
                   double weight, double declaredValue, String destinationAddress, String sendingAddress);

    /** Отправка почтового отправления пользователем */
    @ContractAction
    void sendPostal(String receiver, String type, int postalClass,
                    double weight, double declaredValue, String destinationAddress);

    /** Получение почтового отправления */
    @ContractAction
    void getPostal(String trackNumber);

    /** Отказ от получения отправления */
    @ContractAction
    void refusePostal(String trackNumber);

    /** Отправка денежного перевода */
    @ContractAction
    void sendTransfer(String receiver, double amount, int lifetimeDays);

    /** Получение денежного перевода */
    @ContractAction
    void getTransfer(String transferId);

    /** Отказ от денежного перевода */
    @ContractAction
    void refuseTransfer(String transferId);

    /** Отследить отправление по трек-номеру */
    @ContractAction
    Postal trackDeparture(String trackNumber);

    /** Генерация трек-номера */
    @ContractAction
    String generationTrackId(String sendingIndex, String destinationIndex);

    /** Просмотр активных отправлений пользователя */
    @ContractAction
    List<Postal> getActiveDeparture();

    /** Просмотр истории отправлений пользователя */
    @ContractAction
    List<Postal> getHistoryDeparture();

    /** Получение информации о пользователе */
    @ContractAction
    User getUserInfo(String address);

    /** Изменение персональных данных пользователя */
    @ContractAction
    void setPersonInfo(String name, String homeAddress);
}