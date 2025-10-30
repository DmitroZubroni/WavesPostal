package com.wavesenterprise.app.domain;

public class Postalitem {

    private String trackNumber; //
    private String from; //
    private String to; //
    private String type; //
    private String shippingClass; //
    private double weight; //
    private double declaredValue; //
    private String addressTo; //
    private int postOfficeFrom; //
    private int postOfficeTo; //
    private int currentOffice; //
    private String status; //
    private double totalCost; //

    public Postalitem(String trackNumber, String from, String to, String type, String shippingClass,
                      double weight, double declaredValue, String addressTo,
                      int postOfficeFrom, int postOfficeTo) {
        this.trackNumber = trackNumber;
        this.from = from;
        this.to = to;
        this.type = type;
        this.shippingClass = shippingClass;
        this.weight = weight;
        this.declaredValue = declaredValue;
        this.addressTo = addressTo;
        this.postOfficeFrom = postOfficeFrom;
        this.postOfficeTo = postOfficeTo;
        this.currentOffice = postOfficeFrom;
        this.status = "CREATED";
    }

    public Postalitem() {}

    public void setTrackNumber(String trackNumber) {
        this.trackNumber = trackNumber;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setShippingClass(String shippingClass) {
        this.shippingClass = shippingClass;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setDeclaredValue(double declaredValue) {
        this.declaredValue = declaredValue;
    }

    public void setAddressTo(String addressTo) {
        this.addressTo = addressTo;
    }

    public void setPostOfficeFrom(int postOfficeFrom) {
        this.postOfficeFrom = postOfficeFrom;
    }

    public void setPostOfficeTo(int postOfficeTo) {
        this.postOfficeTo = postOfficeTo;
    }

    public void setCurrentOffice(int currentOffice) {
        this.currentOffice = currentOffice;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public String getTrackNumber() {
        return trackNumber;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getType() {
        return type;
    }

    public String getShippingClass() {
        return shippingClass;
    }

    public double getWeight() {
        return weight;
    }

    public double getDeclaredValue() {
        return declaredValue;
    }

    public String getAddressTo() {
        return addressTo;
    }

    public int getPostOfficeFrom() {
        return postOfficeFrom;
    }

    public int getPostOfficeTo() {
        return postOfficeTo;
    }

    public int getCurrentOffice() {
        return currentOffice;
    }

    public String getStatus() {
        return status;
    }

    public double getTotalCost() {
        return totalCost;
    }



}