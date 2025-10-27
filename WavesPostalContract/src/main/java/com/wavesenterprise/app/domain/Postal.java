package com.wavesenterprise.app.domain;

// Почтовое отправление
public class PostalItem {
    public String trackId;
    public String sender;
    public String receiver;
    public PostalType type;
    public int postalClass;
    public int deliveryDays;
    public double deliveryCost;
    public double weight;
    public double declaredValue;
    public double totalCost;
    public String destinationAddress;
    public String sendingAddress;
    public String status;
    public long createdAt;

    public PostalItem() {}
}