package com.example.philippe.signalandroiddemo.signal;

import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.SessionBuilder;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.UntrustedIdentityException;
import org.whispersystems.libsignal.protocol.CiphertextMessage;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;
import org.whispersystems.libsignal.protocol.SignalMessage;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignalProtocolStore;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore;
import org.whispersystems.libsignal.util.KeyHelper;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Random;

/**
 * Class that wraps signal specific methods.
 */
public class SignalWrapper {

    public static int DEFAULT_DEVICE_ID = 0; // deviceId is always 0 for the demo
    public static int PRE_KEY_COUNT = 10; // in this case, 10 prekeys will be generated.
    public static int SIGNED_PRE_KEY_RANGE = 9999;

    public static SignalUser register(String name) throws InvalidKeyException {
        IdentityKeyPair identityKeyPair = KeyHelper.generateIdentityKeyPair();
        int registrationId  = KeyHelper.generateRegistrationId(false);
        SignalProtocolAddress address = new SignalProtocolAddress(name, DEFAULT_DEVICE_ID);

        SignedPreKeyRecord signedPreKey = generateSignedPreKey(identityKeyPair);
        List<PreKeyRecord> preKeys = generatePreKeys(PRE_KEY_COUNT);

        SignalProtocolStore store = new InMemorySignalProtocolStore(identityKeyPair, registrationId);
        store.storeSignedPreKey(signedPreKey.getId(), signedPreKey);
        for (PreKeyRecord preKey : preKeys) {
            store.storePreKey(preKey.getId(), preKey);
        }

        SignalUser user = new SignalUser();
        user.setName(name);
        user.setRegistrationId(registrationId);
        user.setAddress(address);
        user.setIdentityKeyPair(identityKeyPair);
        user.setSignedPreKey(signedPreKey);
        user.setPreKeys(preKeys);
        user.setStore(store);
        return user;
    }

    private static SignedPreKeyRecord generateSignedPreKey(IdentityKeyPair identityKeyPair) throws InvalidKeyException {
        Random rand = new Random();
        int signedPreKeyId = rand.nextInt(SIGNED_PRE_KEY_RANGE);
        return KeyHelper.generateSignedPreKey(identityKeyPair, signedPreKeyId);
    }

    private static List<PreKeyRecord> generatePreKeys(int count) {
        return KeyHelper.generatePreKeys(1, count);
    }

    public static void initSession(SignalUser me, ChatPartner other) throws Exception {
        SessionBuilder sessionBuilder = new SessionBuilder(me.getStore(), other.getAddress());
        sessionBuilder.process(other.getPreKeyBundle());
    }

    public static SessionCipher createSessionCipher(SignalUser me, ChatPartner other) {
        return new SessionCipher(me.getStore(), other.getAddress());
    }

    public static CiphertextMessage encrypt(SessionCipher sessionCipher, String plaintext) throws UnsupportedEncodingException, UntrustedIdentityException {
        return sessionCipher.encrypt(plaintext.getBytes("UTF-8"));
    }

    public static String decrypt(SessionCipher sessionCipher, CiphertextMessage ciphertext) throws Exception {
        PreKeySignalMessage preKeySignalMessage;
        SignalMessage signalMessage;
        byte[] plaintext;

        if (ciphertext.getType() == CiphertextMessage.PREKEY_TYPE) {
            // Decrypt a PreKeyWhisperMessage by first establishing a new session
            // The session will be set up automatically by libsignal.
            // The information to do that is delivered within the message's ciphertext.
            preKeySignalMessage = new PreKeySignalMessage(ciphertext.serialize());
            plaintext = sessionCipher.decrypt(preKeySignalMessage);

        } else {
            // Decrypt a normal message using an existing session
            signalMessage = new SignalMessage(ciphertext.serialize());
            plaintext = sessionCipher.decrypt(signalMessage);

        }

        return new String(plaintext);
    }

    public static String decrypt(SessionCipher sessionCipher, MessageModel messageModel) throws Exception {
        if (messageModel.getType() == CiphertextMessage.PREKEY_TYPE) {
            return decrypt(sessionCipher, new PreKeySignalMessage(messageModel.getBody()));
        } else {
            return decrypt(sessionCipher, new SignalMessage(messageModel.getBody()));
        }
    }
}
