package com.example.philippe.signalandroiddemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.philippe.signalandroiddemo.signal.ChatPartner;
import com.example.philippe.signalandroiddemo.signal.SignalUser;
import com.example.philippe.signalandroiddemo.signal.SignalWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserListActivity extends AppCompatActivity {
    ArrayList<String> arrayListUsernames;
    OkHttpClient httpClient;
    ArrayAdapter listAdapter;
    JSONArray userlist = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        httpClient = new OkHttpClient();
        arrayListUsernames = new ArrayList<>();

        loadUsernames();
        initListView();
        setTitle("Gefundene Chatpartner");
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        //Klick Methode für den Zurück-Button
        if (id == android.R.id.home) {
            this.finish();
        }
        //Klick Methode für den update-Button
        if (id == R.id.action_update) {
            loadUsernames();
        }
        return super.onOptionsItemSelected(item);
    }

    public void initListView() {
        ListView listViewUsers = findViewById(R.id.listViewUsers);

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayListUsernames);
        listViewUsers.setAdapter(listAdapter);
        listViewUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int userid = -1;
                try {
                    userid = userlist.getJSONObject(position).getInt("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                startChatWithChatPartner(userid);
            }
        });
    }

    private void startChatWithChatPartner(final int chatPartnerId) {
        Request request = new Request.Builder()
                .url(MainActivity.API_URL + "/user/" + chatPartnerId)
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(final Call call, final Response response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String data = response.body().string();
                            Log.e("TAG", "userdataChatpartner " + data);
                            JSONObject userdataChatpartner = new JSONObject(data);
                            ChatPartner chatPartner = new ChatPartner(userdataChatpartner);
                            ((ChatApplication)getApplicationContext()).setCurrentChatPartner(chatPartner);

                            SignalUser currentUser = ((ChatApplication)getApplicationContext()).getCurrentUser();

                            SignalWrapper.initSession(currentUser, chatPartner);

                            Intent showChatActivity = new Intent(getApplicationContext(), ChatActivity.class);
                            startActivity(showChatActivity);
                        } catch (Exception e) {
                            Log.e("Error", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void loadUsernames() {
        Request request = new Request.Builder()
                .url(MainActivity.API_URL + "/users")
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) {
                try {
                    SignalUser currentUser = ((ChatApplication)getApplicationContext()).getCurrentUser();
                    String data = response.body().string();
                    JSONArray users = new JSONArray(data);
                    arrayListUsernames.clear();
                    userlist = new JSONArray();
                    String username;
                    for (int i = 0; i < users.length(); i++) {
                        JSONObject chatpartner = users.getJSONObject(i);
                        username = chatpartner.getString("name");
                        // filter current user, so that he's not shown in the contacts list
                        if (!username.equalsIgnoreCase(currentUser.getName())) {
                            userlist.put(chatpartner);
                            arrayListUsernames.add(username);
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listAdapter.notifyDataSetChanged();
                        }
                    });
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
}
