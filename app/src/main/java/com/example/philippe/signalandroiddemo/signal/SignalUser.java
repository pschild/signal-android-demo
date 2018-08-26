package com.example.philippe.signalandroiddemo.signal;

import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignalProtocolStore;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;

import java.util.List;

public class SignalUser {

    private int id;
    private String name;
    private int registrationId;
    private SignalProtocolAddress address;
    private IdentityKeyPair identityKeyPair;
    private SignedPreKeyRecord signedPreKey;
    private List<PreKeyRecord> preKeys;
    private SignalProtocolStore store;

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

    public IdentityKeyPair getIdentityKeyPair() {
        return identityKeyPair;
    }

    public void setIdentityKeyPair(IdentityKeyPair identityKeyPair) {
        this.identityKeyPair = identityKeyPair;
    }

    public SignedPreKeyRecord getSignedPreKey() {
        return signedPreKey;
    }

    public void setSignedPreKey(SignedPreKeyRecord signedPreKey) {
        this.signedPreKey = signedPreKey;
    }

    public List<PreKeyRecord> getPreKeys() {
        return preKeys;
    }

    public void setPreKeys(List<PreKeyRecord> preKeys) {
        this.preKeys = preKeys;
    }

    public SignalProtocolStore getStore() {
        return store;
    }

    public void setStore(SignalProtocolStore store) {
        this.store = store;
    }
}
