package com.wavesenterprise.app.domain;

// Пользователь системы
public class User {
    public String name;
    public String homeAddress;
    public long balance;
    public Role role;
    public String postOfficeId;

    public User() {}

    public User(String name, String homeAddress, long balance, Role role, String postOfficeId) {
        this.name = name;
        this.homeAddress = homeAddress;
        this.balance = balance;
        this.role = role;
        this.postOfficeId = postOfficeId;
    }
}