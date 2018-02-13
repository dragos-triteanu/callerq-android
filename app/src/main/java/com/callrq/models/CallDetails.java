package com.callrq.models;

import java.io.Serializable;
import java.util.Calendar;

public class CallDetails implements Serializable {

    private String contactName;
    private String phoneNumber;
    private Calendar callStartedTime;
    private Calendar callStopTime;

    public CallDetails(String contactName, String phoneNumber, Calendar callStartedTime, Calendar callStopTime) {
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
        this.callStartedTime = callStartedTime;
        this.callStopTime = callStopTime;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Calendar getCallStartedTime() {
        return callStartedTime;
    }

    public void setCallStartedTime(Calendar callStartedTime) {
        this.callStartedTime = callStartedTime;
    }

    public Calendar getCallStopTime() {
        return callStopTime;
    }

    public void setCallStopTime(Calendar callStopTime) {
        this.callStopTime = callStopTime;
    }
}
