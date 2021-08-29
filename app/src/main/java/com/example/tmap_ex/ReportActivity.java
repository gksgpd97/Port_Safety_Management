package com.example.tmap_ex;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;

import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportActivity extends AppCompatActivity {

    private Context mContext = null;
    private boolean m_bTrackingMode = true;
    private TMapGpsManager tmapgps = null;
    private TMapView tmapview = null;
    private static String mApiKey = "";
    private TMapPoint currentPoint = null;
    private TMapPoint reportPoint = null;
    private double cur_latitude, cur_longitude;
    private double rep_latitude, rep_longitude;
    private String categoryId;
    private RadioGroup radioGroup;
    RequestQueue queue;
    private Toolbar toolbar;
    private double latitude, longitude;

    private ActionBar actionBar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private String TAG = "TAGTOKEN";
    private String token_id;
    Map<String, String> map;
    private RequestQueue tokenQueue;
    private ArrayList<String> token_list = new ArrayList<String>();
    String regToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_activity);

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        sendPushTokenToDB();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("신고하기");
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        navigationView = findViewById(R.id.nav);
        navigationView.setItemIconTintList(null);

        Intent intent= getIntent();

        mContext =this;

        Bundle bundle= getIntent().getExtras();
        if (bundle!= null) {
            latitude = intent.getExtras().getDouble("cur_latitude");
            longitude = intent.getExtras().getDouble("cur_longitude");
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.menu1:
                        //Toast.makeText(mContext, "지도",Toast.LENGTH_LONG).show();
                        Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                        intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent1);
                        break;

                    case R.id.menu2:
                        break;

                    case R.id.menu3:
                        //Toast.makeText(mContext, "통계",Toast.LENGTH_LONG).show();
                        Intent intent3 = new Intent(getApplicationContext(), StatsActivity.class);
                        try{
                            intent3.putExtra("cur_latitude",latitude);
                            Log.d("type", String.valueOf(latitude));
                            intent3.putExtra("cur_longitude",longitude);
                            startActivityForResult(intent3, 3);
                        }catch (Exception e){
                            Toast.makeText(getApplicationContext(),"현재위치를 활성화해주세요.", Toast.LENGTH_LONG).show();
                        }
                        break;
                }

                drawerLayout.closeDrawer(navigationView);
                return false;
            }
        });

        currentPoint = new TMapPoint(latitude, longitude);
        Log.d("넘겨받는 값", currentPoint.getLatitude()+", "+currentPoint.getLongitude());

        ConstraintLayout mapViewLayout = (ConstraintLayout) findViewById(R.id.map_view_layout);
        Button confirmButton = (Button) findViewById(R.id.confirm_button);
        Button cancelButton = (Button) findViewById(R.id.cancel_button);

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        tmapview = new TMapView(this);
        tmapview.setSKTMapApiKey(mApiKey);

        tmapview.setCompassMode(true); //현재보는방향 자이로
        tmapview.setIconVisibility(true);//현재위치로 표시될 아이콘을 표시할지 여부를 설정합니다.
        tmapview.setSightVisible(true); //시야표출여부를 설정
        tmapview.setZoomLevel(18);
        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
        tmapview.setLanguage(TMapView.LANGUAGE_KOREAN);

        tmapview.setLocationPoint(currentPoint.getLongitude(),currentPoint.getLatitude());
        tmapview.setCenterPoint(currentPoint.getLongitude(),currentPoint.getLatitude());
        mapViewLayout.addView(tmapview);

        new BackgroundTask().execute();

        confirmButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                reportPoint = tmapview.getCenterPoint();

                int radioButtonId = radioGroup.getCheckedRadioButtonId();
                switch(radioButtonId){
                    case R.id.radio1: { //첫째 라디오 버튼일 경우
                        categoryId = "1";
                        break;
                    }
                    case R.id.radio2: { //두번째 라디오 버튼일 경우
                        categoryId = "2";
                        break;
                    }
                    case R.id.radio3: { //세번째 라디오 버튼일 경우
                        categoryId = "3";
                        break;
                    }
                    case R.id.radio4: {
                        categoryId = "4";
                        break;
                    }
                    case R.id.radio5: {
                        categoryId = "5";
                        break;
                    }
                    case R.id.radio6: {
                        categoryId = "6";
                        break;
                    }
                    case R.id.radio7: {
                        categoryId = "7";
                        break;
                    }
                }
                rep_latitude = reportPoint.getLatitude();
                rep_longitude = reportPoint.getLongitude();

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                        try {
                            int jsonStart = response.indexOf("{");
                            int jsonEnd = response.lastIndexOf("}");

                            if (jsonStart >= 0 && jsonEnd >= 0 && jsonEnd > jsonStart) {
                                response = response.substring(jsonStart, jsonEnd + 1);
                            }
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            if(success) {
                                Log.d("TAG", "디비성공");
                                finish();
                            }
                            else {
                                /*
                                AlertDialog.Builder builder = new AlertDialog.Builder(ReportActivity.this);
                                dialog = builder.setMessage("변경에 실패했습니다.")
                                        .setNegativeButton("확인", null)
                                        .create();
                                dialog.show();
                                 */
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                Response.ErrorListener errorListener = new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(getApplicationContext(),"디비 처리시 에러발생!",Toast.LENGTH_SHORT).show();
                        Log.d("TAG", String.valueOf(error));
                        return;
                    }
                };


                ReportRequest reportRequest = new ReportRequest(categoryId, Double.toString(rep_latitude), Double.toString(rep_longitude), responseListener, errorListener);
                reportRequest.setShouldCache(false);
                queue = Volley.newRequestQueue(getApplicationContext());
                queue.add(reportRequest);
                for(int count = 0; count < token_list.size(); count++){
                    Log.d("token_list",token_list.get(count));
                    SendNotification.sendNotification(token_list.get(count), "사고가 발생했습니다!!!", "사고지역을 확인해주세요.");
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ // 왼쪽 상단 버튼 눌렀을 때
                drawerLayout.openDrawer(navigationView);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendPushTokenToDB() {
        //파이어베이스
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        token_id = task.getResult().getToken();

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token_id);
                        Log.d(TAG, msg);
                        //Toast.makeText(ReportActivity.this, token_id, Toast.LENGTH_SHORT).show();
                        //finish();
                    }
                });
    }

    class BackgroundTask extends AsyncTask<Void, Void, String> {

        String target;

        @Override
        protected  void onPreExecute() {
            try {
                target = "";
                Log.d(TAG,"try");
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG,"catch");
            }

        }

        @Override
        protected void onPostExecute(String result) {
            try {
                token_list.clear();

                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("response");
                int count = 0;

                String user_id;
                String user_pwd;
                String user_token;

                while(count < jsonArray.length()) {
                    JSONObject object = jsonArray.getJSONObject(count);
                    user_token = object.getString("user_token");

                    token_list.add(user_token);
                    count++;
                }
                Log.d("size", String.valueOf(token_list.size()));
            }
            catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG,"catch2");
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

