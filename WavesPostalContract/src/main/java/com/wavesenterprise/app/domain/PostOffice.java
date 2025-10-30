package com.wavesenterprise.app.domain;

public class PostOffice {

    private int postNumber; //
    private String officeType; //

    public PostOffice(int postNumber, String officeType) {
        this.postNumber = postNumber;
        this.officeType = officeType;
    }

    public PostOffice() {}

    public int getPostNumber() { return postNumber; }
    public void setPostNumber(int postNumber) { this.postNumber = postNumber; }

    public String getOfficeType() { return officeType; }
    public void setOfficeType(String officeType) { this.officeType = officeType; }
}