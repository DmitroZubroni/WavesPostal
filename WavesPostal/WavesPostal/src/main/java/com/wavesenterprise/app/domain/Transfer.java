package com.wavesenterprise.app.domain;

public class Transfer {
    public String id;
    public String sender;
    public String recipient;
    public double amount;
    public String status;

    public Transfer() {}
    public Transfer(String id, String sender, String recipient, double amount) {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.status = "PENDING";
    }
}