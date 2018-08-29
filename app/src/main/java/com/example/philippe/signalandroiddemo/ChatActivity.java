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
import org.json.JSONObject;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.protocol.CiphertextMessage;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayListAllMessages);
        listViewChat.setAdapter(listAdapter);
    }

    public void sendMessage(View view) {
        EditText editTextMessage = findViewById(R.id.editTextMessage);
        String clearMessage = editTextMessage.getText().toString();
        JSONObject jsonObject = new JSONObject();

        try {
            // add outgoing message directly to UI
            arrayListAllMessages.add(currentUser.getName() + ", " + this.getFormattedDate() + ":\n" + clearMessage);
            listAdapter.notifyDataSetChanged();

            // encrypt message
            CiphertextMessage ciphertextMessage = SignalWrapper.encrypt(sessionCipher, clearMessage);

            // prepare JSON object to send to server
            jsonObject.put("sourceRegistrationId", currentUser.getRegistrationId());
            jsonObject.put("recipientRegistrationId", currentChatPartner.getRegistrationId());
            jsonObject.put("body", Base64.encodeToString(ciphertextMessage.serialize(), Base64.NO_WRAP));
            jsonObject.put("type", ciphertextMessage.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        // REST-Request: send message
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
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void updateMessages() {
        //received the sent messages from the chatpartner to the user
        loadMessages(currentChatPartner.getRegistrationId(), currentUser.getRegistrationId());
    }

    private void loadMessages(final int senderRegistrationId, final int recipientRegistrationId) {
        // REST-Request: get messages for current user, sent by user with registrationId senderRegistrationId
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
                } else {
                    try {
                        data = response.body().string();
                        JSONArray messageArray = null;
                        if (data.length() > 0) {
                            messageArray = new JSONArray(data);
                            // loop through all new messages to decrypt them
                            for (int i = 0; i < messageArray.length(); i++) {
                                MessageModel messageModel = new MessageModel(messageArray.getJSONObject(i));
                                // decrypt message
                                String decryptedMessage = SignalWrapper.decrypt(sessionCipher, messageModel);
                                // add decrypted message to UI
                                arrayListAllMessages.add(currentChatPartner.getName() + ", " + messageModel.getTimestamp() + ":\n" + decryptedMessage);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void initActionbar() {
        setTitle(currentChatPartner.getName());
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFormattedDate(){
        Date date = new Date();
        Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }
}
