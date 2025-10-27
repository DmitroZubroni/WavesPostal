package com.wavesenterprise.app.domain;

// Денежный перевод
public class MoneyTransfer {
    public String sender;
    public String receiver;
    public long amount;
    public long lifetimeDays;
    public long createdAt;
    public String status;

    public MoneyTransfer(String sender, String receiver, long amount, long lifetimeDays) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.lifetimeDays = lifetimeDays;
        this.createdAt = System.currentTimeMillis();
        this.status = "PENDING";
    }
}