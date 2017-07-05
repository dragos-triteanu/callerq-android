package com.callerq.models;

import java.io.Serializable;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;

public class Reminder implements Parcelable {

    private static final long serialVersionUID = 5124200187921054968L;

    private long id;
    private int callDuration;
    private long callStartDatetime;
    private long createdDatetime;
    private boolean isMeeting;
    private String memoText;
    private long scheduleDatetime;
    private String contactName;
    private String contactCompany;
    private String contactEmail;
    private List<String> contactPhones;
    private boolean uploaded;

    public Reminder() {
        id = (int) (Math.random() * 2147483647);
        uploaded = false;
    }

    protected Reminder(Parcel in) {
        String[] data = new String[3];

        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.contactName = data[0];
        this.memoText = data[1];
        this.contactCompany = data[2];
        this.contactPhones = in.createStringArrayList();
    }

    public static final Creator<Reminder> CREATOR = new Creator<Reminder>() {
        @Override
        public Reminder createFromParcel(Parcel in) {
            return new Reminder(in);
        }

        @Override
        public Reminder[] newArray(int size) {
            return new Reminder[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getCallDuration() {
        return callDuration;
    }

    public void setCallDuration(int callDuration) {
        this.callDuration = callDuration;
    }

    public long getCallStartDatetime() {
        return callStartDatetime;
    }

    public void setCallStartDatetime(long callStartDatetime) {
        this.callStartDatetime = callStartDatetime;
    }

    public long getCreatedDatetime() {
        return createdDatetime;
    }

    public void setCreatedDatetime(long createdDatetime) {
        this.createdDatetime = createdDatetime;
    }

    public boolean isMeeting() {
        return isMeeting;
    }

    public void setMeeting(boolean isMeeting) {
        this.isMeeting = isMeeting;
    }

    public String getMemoText() {
        return memoText;
    }

    public void setMemoText(String memoText) {
        this.memoText = memoText;
    }

    public long getScheduleDatetime() {
        return scheduleDatetime;
    }

    public void setScheduleDatetime(long scheduleDatetime) {
        this.scheduleDatetime = scheduleDatetime;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactCompany() {
        return contactCompany;
    }

    public void setContactCompany(String contactCompany) {
        this.contactCompany = contactCompany;
    }
    
    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public List<String> getContactPhones() {
        return contactPhones;
    }

    public void setContactPhones(List<String> contactPhones) {
        this.contactPhones = contactPhones;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();

        contentValues.put("_id", id);
        contentValues.put("callDuration", callDuration);
        contentValues.put("callStartTime", callStartDatetime);
        contentValues.put("createdDatetime", createdDatetime);
        contentValues.put("isMeeting", isMeeting ? 1 : 0);
        contentValues.put("memoText", memoText);
        contentValues.put("scheduleDatetime", scheduleDatetime);
        contentValues.put("contactName", contactName);
        contentValues.put("contactCompany", contactCompany);
        contentValues.put("contactEmail", contactEmail);

        String contactPhone1 = new String();
        String contactPhone2 = new String();
        String contactPhone3 = new String();

        int count = 0;
        for (String phone : contactPhones) {
            switch (count) {
            case 0:
                contactPhone1 = phone;
                break;
            case 1:
                contactPhone2 = phone;
                break;
            case 2:
                contactPhone3 = phone;
                break;
            default:
                continue;
            }
        }

        contentValues.put("contactPhone1", contactPhone1);
        contentValues.put("contactPhone2", contactPhone2);
        contentValues.put("contactPhone3", contactPhone3);

        contentValues.put("uploaded", uploaded ? 1 : 0);

        return contentValues;
    }

    public JSONObject toJSONObject() {
        try {
            JSONObject jsonReminder = new JSONObject();
            jsonReminder.put("localId", Long.toString(id));
            JSONObject jsonContact = new JSONObject();
            jsonContact.put("name", contactName);
            jsonContact.put("company", contactCompany);
            jsonContact.put("email", contactEmail);
            JSONArray jsonPhones = new JSONArray();
            for (String phoneNumber : contactPhones) {
                jsonPhones.put(phoneNumber);
            }
            jsonContact.put("phoneNumbers", jsonPhones);
            jsonReminder.put("contact", jsonContact);
            jsonReminder.put("memoText", memoText);
            jsonReminder.put("createdDateTime", createdDatetime);
            jsonReminder.put("callStartDateTime", callStartDatetime);
            jsonReminder.put("callDuration", callDuration);
            jsonReminder.put("isMeeting", isMeeting);
            jsonReminder.put("scheduledDateTime", scheduleDatetime);

            return jsonReminder;

        } catch (JSONException e) {
            return null;
        }

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[] {
                this.contactName,
                this.memoText,
                this.contactCompany
        });
        parcel.writeList(this.contactPhones);
    }

}
