package com.example.philippe.signalandroiddemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.philippe.signalandroiddemo.signal.SignalUser;
import com.example.philippe.signalandroiddemo.signal.SignalWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.state.PreKeyRecord;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String API_URL = "http://192.168.178.20:8081";

    EditText editTextName;
    EditText editTextPassword;

    OkHttpClient httpClient;
    MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextName = findViewById(R.id.editTextName);
        editTextPassword = findViewById(R.id.editTextPassword);

        httpClient = new OkHttpClient();
    }

    public void register(View view) {
        SignalUser user = null;
        try {
            user = SignalWrapper.register(editTextName.getText().toString());
            sendUserDataToDatabase(user);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            Toast.makeText(this, "Fehler beim Erzeugen der Schlüssel", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Fehler bei der Registrierung", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendUserDataToDatabase(SignalUser user) throws JSONException {
        /*send userdata to DB*/

        /*
          Create a JSON Object with the following structure:
          {
               "username": String,
               "preKeyBundle": {
                   "identityKey": String,
                   "signedPreKey": {
                       "keyId": Integer,
                       "publicKey": String,
                       "signature": String
                   },
                   "registrationId": Integer,
                   "preKeys": [
                       {
                           "keyId": Integer,
                           "publicKey": String
                       },
                       ...
                   ]
               }
          }
         */

        JSONObject spk = new JSONObject();
        spk.put("keyId", user.getSignedPreKey().getId());
        spk.put("publicKey", Base64.encodeToString(user.getSignedPreKey().getKeyPair().getPublicKey().serialize(), Base64.NO_WRAP));
        spk.put("signature", Base64.encodeToString(user.getSignedPreKey().getSignature(), Base64.NO_WRAP));

        JSONArray pks = new JSONArray();
        JSONObject pk;
        for (PreKeyRecord preKey : user.getPreKeys()) {
            pk = new JSONObject();
            pk.put("keyId", preKey.getId());
            pk.put("publicKey", Base64.encodeToString(preKey.getKeyPair().getPublicKey().serialize(), Base64.NO_WRAP));
            pks.put(pk);
        }

        JSONObject pkb = new JSONObject();
        pkb.put("identityKey", Base64.encodeToString(user.getIdentityKeyPair().getPublicKey().serialize(), Base64.NO_WRAP));
        pkb.put("signedPreKey", spk);
        pkb.put("registrationId", user.getRegistrationId());
        pkb.put("preKeys", pks);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", user.getName());
        jsonObject.put("preKeyBundle", pkb);

        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        Request request = new Request.Builder()
                .url(this.API_URL + "/user")
                .post(body)
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String userdata = response.body().string();
                            Log.e("TAG", "sendUserDataToDatabase() userdata from response" +userdata);
                            Intent showUserListActivity = new Intent(getApplicationContext(), UserListActivity.class);
                            showUserListActivity.putExtra("userdata", userdata);
                            startActivity(showUserListActivity);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }


}
