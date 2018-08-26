package com.example.philippe.signalandroiddemo;

import android.app.Application;

import com.example.philippe.signalandroiddemo.signal.ChatPartner;
import com.example.philippe.signalandroiddemo.signal.SignalUser;

public class ChatApplication extends Application {

    private SignalUser currentUser;

    private ChatPartner currentChatPartner;

    public SignalUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(SignalUser currentUser) {
        this.currentUser = currentUser;
    }

    public ChatPartner getCurrentChatPartner() {
        return currentChatPartner;
    }

    public void setCurrentChatPartner(ChatPartner currentChatPartner) {
        this.currentChatPartner = currentChatPartner;
    }
}
