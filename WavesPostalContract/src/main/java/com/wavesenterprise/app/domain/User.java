package com.wavesenterprise.app.domain;

public class User {
    private String name; //
    private String homeAddress; //
    private String blockchainAddress; //
    private double balance; //
    private String role; //
    private String postId; //

    public User(String name, String homeAddress, String blockchainAddress, double balance, String role) {
        this.name = name;
        this.homeAddress = homeAddress;
        this.blockchainAddress = blockchainAddress;
        this.balance = balance;
        this.role = role;
    }

    public User() {}

    public void setName(String name) {
        this.name = name;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public void setBlockchainAddress(String blockchainAddress) {
        this.blockchainAddress = blockchainAddress;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = "RR" + postId; }

    public String getName() {
        return name;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public String getBlockchainAddress() {
        return blockchainAddress;
    }

    public double getBalance() {
        return balance;
    }

    public String getRole() {
        return role;
    }
}