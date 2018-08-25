package com.example.philippe.signalandroiddemo;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.philippe.signalandroiddemo.signal.SignalWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.whispersystems.libsignal.logging.Log;
import org.whispersystems.libsignal.protocol.CiphertextMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {
    String chatPartnerName;
    int chatPartnerId;
    ArrayList<String> arrayListReceivedMessages = new ArrayList<>();
    ArrayList<String> arrayListSentMessages = new ArrayList<>();
    ArrayList<String> arrayListAllMessages = new ArrayList<>();
    ArrayAdapter listAdapter;
    ListView listViewChat;

    OkHttpClient httpClient;
    MediaType JSON = MediaType.get("application/json; charset=utf-8");

    JSONObject userdataUser;
    JSONObject userdataChatpartner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        httpClient = new OkHttpClient();
        getDataFromIntent();

        getDataFromChatPartner();

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
            arrayListSentMessages.add(userdataUser.getString("name") + ": " + clearMessage);
            updateMessageList();

            // TODO: sessionCipher
//            CiphertextMessage ciphertextMessage = SignalWrapper.encrypt(userdataChatpartner.getInt("id"), clearMessage);

            jsonObject.put("sourceRegistrationId", userdataUser.getInt("id"));
            jsonObject.put("recipientRegistrationId", userdataChatpartner.getInt("id"));
//            jsonObject.put("body", ciphertextMessage.serialize());
//            jsonObject.put("type", ciphertextMessage.getType());
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
        try {
            //Log.e("userdata", "updateMessages: " + userdataUser.toString());
            int userid = userdataUser.getInt("id");
            int chatpartnerid = userdataChatpartner.getInt("id");

            //received the sent messages from the chatpartner to the user
            loadMessages(chatpartnerid, userid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadMessages(final int senderid, final int recipientid) {
        Log.e("TAG", "sender: " + senderid + ", recipient " + recipientid);


        Request request = new Request.Builder()
                .url(MainActivity.API_URL + "/messages/" + senderid + "/" + recipientid)
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
                                JSONObject messageObject = messageArray.getJSONObject(i);
//                                decrypt(userdataUser.getString("name"), userdataUser.getString("password"));
                                String decryptedMessage = "";
                                arrayListReceivedMessages.add(userdataChatpartner.getString("name") + ": " + decryptedMessage);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
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

    //reading the chatpartner and the userdata from the activity before
    private void getDataFromIntent() {
        Intent intent = getIntent();
        chatPartnerName = intent.getStringExtra("chatPartnerName");
        chatPartnerId = intent.getIntExtra("chatPartnerId", -1);
        try {
            userdataUser = new JSONObject(intent.getStringExtra("userdata"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initActionbar() {
        setTitle(chatPartnerName);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getDataFromChatPartner() {
        Request request = new Request.Builder()
                .url(MainActivity.API_URL + "/user/" + chatPartnerId)
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
                            String data = response.body().string();
                            Log.e("TAG", "userdataChatpartner " + data);
                            userdataChatpartner = new JSONObject(data);
                        } catch (Exception e) {
                            Log.e("Error", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}
