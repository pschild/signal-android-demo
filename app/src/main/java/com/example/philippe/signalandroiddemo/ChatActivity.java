package com.example.philippe.signalandroiddemo;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.philippe.signalandroiddemo.signal.ChatPartner;
import com.example.philippe.signalandroiddemo.signal.MessageModel;
import com.example.philippe.signalandroiddemo.signal.SignalUser;
import com.example.philippe.signalandroiddemo.signal.SignalWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.protocol.CiphertextMessage;
import org.whispersystems.libsignal.protocol.SignalMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private SignalUser currentUser;
    private ChatPartner currentChatPartner;

    private SessionCipher sessionCipher;

    ArrayList<String> arrayListReceivedMessages = new ArrayList<>();
    ArrayList<String> arrayListSentMessages = new ArrayList<>();
    ArrayList<String> arrayListAllMessages = new ArrayList<>();
    ArrayAdapter listAdapter;
    ListView listViewChat;

    OkHttpClient httpClient;
    MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        httpClient = new OkHttpClient();

        currentUser = ((ChatApplication)getApplicationContext()).getCurrentUser();
        currentChatPartner = ((ChatApplication)getApplicationContext()).getCurrentChatPartner();
        sessionCipher = SignalWrapper.createSessionCipher(currentUser, currentChatPartner);

        initActionbar();

        initList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.update_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //Klick Methode für den Zurück-Nutton
        if (id == android.R.id.home) {
            this.finish();
        }
        //Klick Methode für den update-Nutton
        if (id == R.id.action_update) {
            Log.e("TAG", "update Menu item clicked");
            updateMessages();

        }
        return super.onOptionsItemSelected(item);
    }

    private void initList() {
        listViewChat = findViewById(R.id.listViewChat);

//        messages = arrayListReceivedMessages.toArray(new String[0]);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayListAllMessages);
        listViewChat.setAdapter(listAdapter);
    }

    public void sendMessage(View view) {
        EditText editTextMessage = findViewById(R.id.editTextMessage);
        String clearMessage = editTextMessage.getText().toString();
        JSONObject jsonObject = new JSONObject();

        try {
            /*save in sendList*/
            arrayListSentMessages.add(currentUser.getName() + ": " + clearMessage);
            updateMessageList();

            CiphertextMessage ciphertextMessage = SignalWrapper.encrypt(sessionCipher, clearMessage);

            jsonObject.put("sourceRegistrationId", currentUser.getRegistrationId());
            jsonObject.put("recipientRegistrationId", currentChatPartner.getRegistrationId());
            jsonObject.put("body", Base64.encodeToString(ciphertextMessage.serialize(), Base64.NO_WRAP));
            jsonObject.put("type", ciphertextMessage.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("TAG", "sendMessage() ich schicke ab: " + jsonObject.toString());
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        Request request = new Request.Builder()
                .url(MainActivity.API_URL + "/message")
                .post(body)
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String responseString = response.body().string();
                            Log.e("TAG", "sendMessage() responseString " + responseString);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void updateMessages() {
        arrayListReceivedMessages.clear();
        //received the sent messages from the chatpartner to the user
        loadMessages(currentChatPartner.getRegistrationId(), currentUser.getRegistrationId());
    }

    private void loadMessages(final int senderRegistrationId, final int recipientRegistrationId) {
        Log.e("TAG", "sender: " + senderRegistrationId + ", recipient " + recipientRegistrationId);


        Request request = new Request.Builder()
                .url(MainActivity.API_URL + "/messages/" + senderRegistrationId + "/" + recipientRegistrationId)
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("TAG", e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) {
                final String data;
                if (response.code() == 204) {
                    Toast.makeText(ChatActivity.this, "Keine Nachrichten", Toast.LENGTH_SHORT).show();
                    Log.e("TAG", "loadMessages() Statuscode " + response.code());
                } else {
                    try {
                        data = response.body().string();
                        //Log.e("arraylist", data);
                        JSONArray messageArray = null;
                        if (data.length() > 0) {
                            messageArray = new JSONArray(data);
                            for (int i = 0; i < messageArray.length(); i++) {
                                MessageModel messageModel = new MessageModel(messageArray.getJSONObject(i));
                                String decryptedMessage = SignalWrapper.decrypt(sessionCipher, messageModel);
                                arrayListReceivedMessages.add(currentChatPartner.getName() + ": " + decryptedMessage);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateMessageList();
                    }
                });
            }
        });
    }

    private void updateMessageList() {
        arrayListAllMessages.clear();
        arrayListAllMessages.addAll(arrayListReceivedMessages);
        arrayListAllMessages.addAll(arrayListSentMessages);

        Collections.sort(arrayListAllMessages, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });

        Log.e("TAG", "updateMessageList() arrayListReceivedMessages: " + arrayListReceivedMessages.toString());
        Log.e("TAG", "updateMessageList() arrayListSentMessages: " + arrayListSentMessages.toString());
        Log.e("TAG", "updateMessageList() arrayListReceivedMessages: " + arrayListAllMessages.toString());
        listAdapter.notifyDataSetChanged();
    }

    private void initActionbar() {
        setTitle(currentChatPartner.getName());
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
