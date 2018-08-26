package com.example.philippe.signalandroiddemo.signal;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageModel {

    private int id;
    private int sourceRegistrationId;
    private int recipientRegistrationId;
    private byte[] body;
    private int type;
    private String timestamp;
    private int fetched;

    public MessageModel(JSONObject jsonObject) {
        try {
            this.id = jsonObject.getInt("id");
            this.sourceRegistrationId = jsonObject.getInt("sourceRegistrationId");
            this.recipientRegistrationId = jsonObject.getInt("recipientRegistrationId");
            this.body = Base64.decode(jsonObject.getString("body").getBytes(), Base64.NO_WRAP);
            this.type = jsonObject.getInt("type");
            this.timestamp = jsonObject.getString("timestamp");
            this.fetched = jsonObject.getInt("fetched");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSourceRegistrationId() {
        return sourceRegistrationId;
    }

    public void setSourceRegistrationId(int sourceRegistrationId) {
        this.sourceRegistrationId = sourceRegistrationId;
    }

    public int getRecipientRegistrationId() {
        return recipientRegistrationId;
    }

    public void setRecipientRegistrationId(int recipientRegistrationId) {
        this.recipientRegistrationId = recipientRegistrationId;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getFetched() {
        return fetched;
    }

    public void setFetched(int fetched) {
        this.fetched = fetched;
    }

    public String toString() {
        return "[" + this.id + "," + this.sourceRegistrationId + "," + this.recipientRegistrationId + "," + this.body + "," + this.type + "," + this.timestamp + "," + this.fetched + "]";
    }
}
