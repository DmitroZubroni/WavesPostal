package com.wavesenterprise.app.domain;

public class MoneyTransfer {

    private String from; //
    private String to; //
    private double amount; //
    private int lifeTime; //
    private boolean active = true; //

    public MoneyTransfer(String from, String to, double amount, int lifeTime) {
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.lifeTime = lifeTime;
    }

    public MoneyTransfer() {}

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public double getAmount() {
        return amount;
    }

    public int getLifeTime() {
        return lifeTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setLifeTime(int lifeTime) {
        this.lifeTime = lifeTime;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}