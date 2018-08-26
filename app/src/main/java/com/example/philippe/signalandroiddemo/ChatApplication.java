package com.example.philippe.signalandroiddemo;

import android.app.Application;

import com.example.philippe.signalandroiddemo.signal.SignalUser;

public class ChatApplication extends Application {

    private SignalUser currentUser;

    public SignalUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(SignalUser currentUser) {
        this.currentUser = currentUser;
    }
}
