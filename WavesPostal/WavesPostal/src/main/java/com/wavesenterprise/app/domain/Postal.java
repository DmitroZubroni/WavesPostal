package com.wavesenterprise.app.domain;

import java.util.ArrayList;
import java.util.List;

public class Postal {
    public String track; // трэк номер
    public String sender; // отправитель
    public String recipient; // получаетль
    public double weight; // вес
    public double declaredValue; // стоимость доставки
    public double cost; // цена
    public String fromIndex; // адрес отправления
    public String toIndex; // адрес назначения
    public String status; // статус
    public List<String> history = new ArrayList<>();

    public Postal() {}
    public Postal(String track, String sender, String recipient, double weight, double declaredValue, double cost,
                  String fromIndex, String toIndex) {
        this.track = track;
        this.sender = sender;
        this.recipient = recipient;
        this.weight = weight;
        this.declaredValue = declaredValue;
        this.cost = cost;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.status = "CREATED";
        this.history.add(fromIndex);
    }
}
