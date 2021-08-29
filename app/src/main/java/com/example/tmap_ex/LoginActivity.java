package com.example.tmap_ex;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private AlertDialog dialog;
    private EditText idText;
    private EditText passwordText;
    private Button loginButton;
    public static String user_id;

    private RequestQueue queue;
    String user_token;
    String get_token;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        sendPushTokenToDB();


        //아마 자동로그인인데 테스트할동안만 활성화
        if(SaveSharedPreference.getUserName(LoginActivity.this).length() != 0) {
            user_id = SaveSharedPreference.getUserName(LoginActivity.this);
            new BackgroundTask().execute();

            /*


            }*/
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            this.finish();
        }
        else{
            //첫 설치및 로그인이라는 의미로 토큰값을 새롭게 저장해야한다.
            //gettoken, 로그인벨리데잇할때 유저아이디랑 디비에 저장
        }

        idText = (EditText) findViewById(R.id.idText);
        passwordText = (EditText) findViewById(R.id.passwordText);
        loginButton = (Button) findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginValidate();
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    public void loginValidate() {
        user_id = idText.getText().toString();
        String user_pwd = passwordText.getText().toString();
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    if(success) {
                        SaveSharedPreference.setUserName(LoginActivity.this, user_id);

                        queue = Volley.newRequestQueue(LoginActivity.this);
                        String url = "";

                        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("user_token", get_token);
                                params.put("user_id", user_id);

                                return params;
                            }
                        };
                        queue.add(stringRequest);
                        Log.d("TAG: ","send token");

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        dialog = builder.setMessage("다시 시도해주세요.")
                                .setNegativeButton("확인", null)
                                .create();
                        dialog.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        LoginRequest loginRequest = new LoginRequest(user_id, user_pwd, get_token, responseListener);
        RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
        queue.add(loginRequest);
    }


    private void sendPushTokenToDB() {
        //파이어베이스
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TAG: ", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        get_token = task.getResult().getToken();

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, get_token);
                        Log.d("TAG: ", msg);
                        //Toast.makeText(LoginActivity.this, get_token, Toast.LENGTH_SHORT).show();
                        //finish();
                    }
                });
    }

    class BackgroundTask extends AsyncTask<Void, Void, String> {

        String target;

        @Override
        protected  void onPreExecute() {
            try {
                Log.d("TAG: ",user_id);
                target = "" + URLEncoder.encode(user_id, "UTF-8");
                Log.d("TAG: ","try");
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("TAG: ","catch");
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("response");
                int count = 0;

                while(count < jsonArray.length()) {
                    JSONObject object = jsonArray.getJSONObject(count);
                    user_token = object.getString("user_token_key");
                    count++;
                }
                Log.d("TAG token: ", user_token);

                if(!user_token.equals(get_token)) {
                    queue = Volley.newRequestQueue(LoginActivity.this);
                    String url = "";

                    final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("user_token", get_token);
                            params.put("user_id", user_id);

                            return params;
                        }
                    };
                    queue.add(stringRequest);
                    Log.d("TAG: ","send token");
                }
                else{
                    Log.d("TAG: ","same token");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                Log.d("TAG: ","catch2");
            }
        }

        @Override
        protected String doInBackground(Void... voids) {

            try {
                URL url = new URL(target);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();

                InputStream inputStream = httpURLConnection.getInputStream();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String temp;

                StringBuilder stringBuilder = new StringBuilder();

                while((temp = bufferedReader.readLine()) != null) {
                    stringBuilder.append(temp+"\n");
                }

                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return stringBuilder.toString().trim();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}
