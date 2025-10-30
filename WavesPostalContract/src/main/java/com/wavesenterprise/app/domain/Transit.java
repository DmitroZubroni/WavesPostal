package com.wavesenterprise.app.domain;

public class Transit {

    private String employeeAddress; //
    private String trackNumber; //
    private double weight; //
    private int officeId; //
    private String action; //
    private long timestamp; //

    public Transit(String employeeAddress, String trackNumber, double weight, int officeId, String action) {
        this.employeeAddress = employeeAddress;
        this.trackNumber = trackNumber;
        this.weight = weight;
        this.officeId = officeId;
        this.action = action;
        this.timestamp = System.currentTimeMillis();
    }

    public void setEmployeeAddress(String employeeAddress) {
        this.employeeAddress = employeeAddress;
    }

    public void setTrackNumber(String trackNumber) {
        this.trackNumber = trackNumber;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setOfficeId(int officeId) {
        this.officeId = officeId;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getEmployeeAddress() {
        return employeeAddress;
    }

    public String getTrackNumber() {
        return trackNumber;
    }

    public double getWeight() {
        return weight;
    }

    public int getOfficeId() {
        return officeId;
    }

    public String getAction() {
        return action;
    }

    public long getTimestamp() {
        return timestamp;
    }
}