package com.wavesenterprise.app.api;

import com.wavesenterprise.sdk.contract.api.annotation.ContractAction;
import com.wavesenterprise.sdk.contract.api.annotation.ContractInit;
import com.wavesenterprise.app.domain.*;

public interface IPostalContract {

    @ContractInit
    void init();

    // Управление пользователями
    @ContractAction
    void registerUser(String name, String homeAddress);

    @ContractAction
    void manageEmployee(String employeeAddress, int postOfficeId, boolean isAdd);

    // Почтовые отправления
    @ContractAction
    void sendPostal(String toAddress, String type, String shippingClass,
                    double weight, double declaredValue, String addressTo,
                    int postOfficeFrom, int postOfficeTo);

    @ContractAction
    void processParcel(String parcelId, int nextPostOfficeId);

    @ContractAction
    void receiveParcel(String parcelId);

    @ContractAction
    void denyParcel(String parcelId);

    // Денежные переводы
    @ContractAction
    void sendMoneyTransfer(String toAddress, double amount, int lifeTimeDays);

    @ContractAction
    void acceptMoneyTransfer(String transferId);

    @ContractAction
    void cancelMoneyTransfer(String transferId);

    // Общие функции
    @ContractAction
    void updatePersonalInfo(String name, String homeAddress);

    @ContractAction
    String trackParcel(String trackNumber);

    // Константы
    class Role {
        public static final String USER = "USER";
        public static final String EMPLOYEE = "EMPLOYEE";
        public static final String ADMIN = "ADMIN";
    }

    class ParcelType {
        public static final String LETTER = "LETTER";
        public static final String BANDEROL = "BANDEROL";
        public static final String PACKAGE = "PACKAGE";
    }

    class ShippingClass {
        public static final String CLASS_1 = "CLASS_1";
        public static final String CLASS_2 = "CLASS_2";
        public static final String CLASS_3 = "CLASS_3";
    }

    class ParcelStatus {
        public static final String CREATED = "CREATED";
        public static final String IN_TRANSIT = "IN_TRANSIT";
        public static final String DELIVERED = "DELIVERED";
        public static final String RECEIVED = "RECEIVED";
        public static final String DENIED = "DENIED";
    }

    class Keys {
        public static final String USERS_PREFIX = "USER_";
        public static final String PARCELS_PREFIX = "PARCEL_";
        public static final String TRANSFERS_PREFIX = "TRANSFER_";
        public static final String OFFICES_PREFIX = "OFFICE_";
        public static final String PARCEL_COUNTER = "PARCEL_COUNT";
        public static final String TRANSFER_COUNTER = "TRANSFER_COUNT";
        public static final String ADMIN_ADDRESS = "ADMIN_ADDRESS";
    }
}