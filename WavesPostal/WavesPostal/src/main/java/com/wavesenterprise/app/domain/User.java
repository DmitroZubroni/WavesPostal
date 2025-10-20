package com.wavesenterprise.app.domain;

public class User {
    public String address;
    public String name;
    public Role role;
    public double balance;
    public String postOfficeId;  // ID почтового отделения (только для сотрудников)

    public User() {}

    public User(String address, String name, Role role, double balance) {
        this.address = address;
        this.name = name;
        this.role = role;
        this.balance = balance;
    }
}
