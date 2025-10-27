package com.wavesenterprise.app.api;

import com.wavesenterprise.app.domain.PostalType;
import com.wavesenterprise.sdk.contract.api.state.ContractState;

/**
 * Контракт почтовой системы - все методы по ТЗ
 */
public interface IPostalContract {

    // Инициализация системы
    @ContractInit
    void init();

    // Управление пользователями
    @ContractAction
    void registerUser(String name, String homeAddress);

    @ContractAction
    void addEmployee(String userAddress, String postOfficeId);

    @ContractAction
    void removeEmployee(String userAddress);

    @ContractAction
    void setEmployeePostOffice(String userAddress, String newPostOfficeId);

    // Почтовые отправления
    @ContractAction
    String createPostalItem(String receiver, PostalType type, int postalClass,
                            double weight, double declaredValue, String destinationAddress, String sendingAddress);

    @ContractAction
    void receivePostalItem(String trackId);

    @ContractAction
    void rejectPostalItem(String trackId);

    // Денежные переводы
    @ContractAction
    String sendMoneyTransfer(String receiver, long amount, long lifetimeDays);

    @ContractAction
    void acceptMoneyTransfer(String transferId);

    @ContractAction
    void rejectMoneyTransfer(String transferId);

    @ContractAction
    void cancelMoneyTransfer(String transferId);

    // Личный кабинет
    @ContractAction
    void updatePersonalInfo(String name, String homeAddress);

    @ContractAction
    Object getPostalHistory();

    @ContractAction
    Object trackPostalItem(String trackId);

    @ContractAction
    Object getActivePostalItems();

    @ContractAction
    Object getAllPostals(); // для администратора
}