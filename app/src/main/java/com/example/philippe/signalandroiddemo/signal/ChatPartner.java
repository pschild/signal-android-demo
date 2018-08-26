package com.example.philippe.signalandroiddemo.signal;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;
import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.state.PreKeyBundle;

public class ChatPartner {

    private int id;
    private String name;
    private int registrationId;
    private SignalProtocolAddress address;
    private byte[] publicIdentityKey;
    private byte[] publicSignedPreKey;
    private int signedPreKeyId;
    private byte[] signature;
    private byte[] publicPreKey;
    private int preKeyId;

    public ChatPartner() { }

    public ChatPartner(JSONObject userData) {
        try {
            id = userData.getInt("id");
            name = userData.getString("name");
            registrationId = userData.getInt("registrationId");
            address = new SignalProtocolAddress(this.name, 0);
            publicIdentityKey = Base64.decode(userData.getString("identityKey"), Base64.NO_WRAP);
            publicSignedPreKey = Base64.decode(userData.getString("pubSignedPreKey"), Base64.NO_WRAP);
            signedPreKeyId = userData.getInt("signedPreKeyId");
            signature = Base64.decode(userData.getString("signature"), Base64.NO_WRAP);

            JSONObject preKey = userData.getJSONObject("preKey");
            publicPreKey = Base64.decode(preKey.getString("pubPreKey"), Base64.NO_WRAP);
            preKeyId = preKey.getInt("keyId");
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public SignalProtocolAddress getAddress() {
        return address;
    }

    public void setAddress(SignalProtocolAddress address) {
        this.address = address;
    }

    public byte[] getPublicIdentityKey() {
        return publicIdentityKey;
    }

    public void setPublicIdentityKey(byte[] publicIdentityKey) {
        this.publicIdentityKey = publicIdentityKey;
    }

    public byte[] getPublicSignedPreKey() {
        return publicSignedPreKey;
    }

    public void setPublicSignedPreKey(byte[] publicSignedPreKey) {
        this.publicSignedPreKey = publicSignedPreKey;
    }

    public int getSignedPreKeyId() {
        return signedPreKeyId;
    }

    public void setSignedPreKeyId(int signedPreKeyId) {
        this.signedPreKeyId = signedPreKeyId;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public byte[] getPublicPreKey() {
        return publicPreKey;
    }

    public void setPublicPreKey(byte[] publicPreKey) {
        this.publicPreKey = publicPreKey;
    }

    public int getPreKeyId() {
        return preKeyId;
    }

    public void setPreKeyId(int preKeyId) {
        this.preKeyId = preKeyId;
    }

    public PreKeyBundle getPreKeyBundle() throws InvalidKeyException {
        return new PreKeyBundle(
                registrationId,
                0,
                preKeyId,
                Curve.decodePoint(publicPreKey, 0),
                signedPreKeyId,
                Curve.decodePoint(publicSignedPreKey, 0),
                signature,
                new IdentityKey(publicIdentityKey, 0)
            );
    }
}
