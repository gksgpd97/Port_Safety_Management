package com.example.tmap_ex;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.graphics.PointF;
import android.location.Location;
import android.os.AsyncTask;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.skt.Tmap.TMapCircle;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.TmapAuthentication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.LogManager;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;



public class MainActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {

    private Context mContext = null;
    private Toolbar toolbar;
    private boolean m_bTrackingMode = true;
    private TMapGpsManager tmapgps = null;
    private TMapView tmapview = null;
    private TMapPoint currentPoint = null;
    private static String mApiKey = "l7xx54970a28096b40faaf92b3017b524f8c";
    private static int mMarkerID;
    private String mJsonString;
    private static final String TAG_JSON="webnautes";
    private ArrayList<TMapMarkerItem> arr = new ArrayList<>();
    private TMapPoint reportPoint = null;
    private double latitude, longitude;
    private String add;

    NavigationView navigationView;
    DrawerLayout drawerLayout;

    @Override
    public void onLocationChange(Location location){
        if(m_bTrackingMode){
            //Log.d("tag", "onlocationchange 불렸음");
            tmapview.setLocationPoint(location.getLongitude(),location.getLatitude());
            currentPoint = tmapgps.getLocation();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("항만사고알리미");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        navigationView = findViewById(R.id.nav);
        navigationView.setItemIconTintList(null);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.menu1:
                        Toast.makeText(mContext, "지도",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    case R.id.menu2:
                        Toast.makeText(mContext, "신고",Toast.LENGTH_LONG).show();
                        Intent intent2 = new Intent(getApplicationContext(), ReportActivity.class);
                        startActivity(intent2);
                    case R.id.menu3:
                        Toast.makeText(mContext, "통계",Toast.LENGTH_LONG).show();
                        Intent intent3 = new Intent(getApplicationContext(), StatsActivity.class);
                        startActivity(intent3);
                }

                drawerLayout.closeDrawer(navigationView);
                return false;
            }
        });

        ConstraintLayout mapViewLayout = (ConstraintLayout) findViewById(R.id.map_view_layout);
        Button rp_btn = (Button)findViewById(R.id.report_btn);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        tmapview = new TMapView(this);
        tmapview.setSKTMapApiKey(mApiKey);

        tmapview.setCompassMode(true); //현재보는방향 자이로
        tmapview.setIconVisibility(true);//현재위치로 표시될 아이콘을 표시할지 여부를 설정합니다.
        tmapview.setSightVisible(true); //시야표출여부를 설정
        tmapview.setZoomLevel(18);
        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
        tmapview.setLanguage(TMapView.LANGUAGE_KOREAN);

        tmapgps = new TMapGpsManager(MainActivity.this);
        tmapgps.setMinTime(1000);
        tmapgps.setMinDistance(5);
        tmapgps.setProvider(tmapgps.NETWORK_PROVIDER);//연결된 인터넷으로 현 위치를 받음. 실내일 때 사용.
        //tmapgps.setProvider(tmapgps.GPS_PROVIDER); //gps로 현 위치를 받음. 실외일때 사용가능.
        tmapgps.OpenGps();

        tmapview.setTrackingMode(true);
        mapViewLayout.addView(tmapview);

        tmapview.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback(){
            @Override
            public void onCalloutRightButton(TMapMarkerItem tMapMarkerItem){
                if(tMapMarkerItem.getID().charAt(0) == 'n' && tMapMarkerItem.getID().charAt(1) == 'r'){ //not solved report marker
                    TMapPoint targetPoint = tMapMarkerItem.getTMapPoint();
                    final TMapMarkerItem targetMarker = tMapMarkerItem;
                    final double targetLatitude = targetPoint.getLatitude();
                    final double targetLongitude = targetPoint.getLongitude();
                    Log.d("TAG", ": " + tMapMarkerItem.getID() +" " + targetLatitude + " " + targetLongitude);
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("사고 처리 완료");
                    builder.setMessage("완료하시겠습니까?");
                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Toast.makeText(getApplicationContext(),"예를 선택했습니다.",Toast.LENGTH_LONG).show();
                            Response.Listener<String> responseListener = new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject jsonResponse = new JSONObject(response);
                                        boolean success = jsonResponse.getBoolean("success");
                                        if(success) {
                                            //Toast.makeText(mContext, "성공", Toast.LENGTH_SHORT).show();
                                            targetMarker.setCanShowCallout(false);
                                            onStart();
                                        }
                                        else {
                                            Toast.makeText(mContext, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            Response.ErrorListener errorListener = new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(getApplicationContext(),"에러발생",Toast.LENGTH_SHORT).show();
                                    Log.d("TAG", String.valueOf(error));
                                    return;
                                }
                            };
                            StateUpdateRequest stateUpdateRequest = new StateUpdateRequest(Double.toString(targetLatitude), Double.toString(targetLongitude), responseListener, errorListener);
                            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                            queue.add(stateUpdateRequest);
                        }
                    });
                    builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Toast.makeText(getApplicationContext(),"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.show();
                }

                if(tMapMarkerItem.getID().charAt(0) == 'd' && tMapMarkerItem.getID().charAt(1) == 'g'){ //detected gas marker
                    TMapPoint targetPoint = tMapMarkerItem.getTMapPoint();
                    final TMapMarkerItem targetMarker = tMapMarkerItem;
                    final double targetLatitude = targetPoint.getLatitude();
                    final double targetLongitude = targetPoint.getLongitude();
                    Log.d("TAG", ": " + tMapMarkerItem.getID() +" " + targetLatitude + " " + targetLongitude);
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("가스누출사고 처리 완료");
                    builder.setMessage("완료하시겠습니까?");
                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Toast.makeText(getApplicationContext(),"예를 선택했습니다.",Toast.LENGTH_LONG).show();
                            Response.Listener<String> responseListener = new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject jsonResponse = new JSONObject(response);
                                        boolean success = jsonResponse.getBoolean("success");
                                        if(success) {
                                            //Toast.makeText(mContext, "성공", Toast.LENGTH_SHORT).show();
                                            targetMarker.setCanShowCallout(false);
                                            onStart();
                                        }
                                        else {
                                            Toast.makeText(mContext, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            Response.ErrorListener errorListener = new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(getApplicationContext(),"에러발생",Toast.LENGTH_SHORT).show();
                                    Log.d("TAG", String.valueOf(error));
                                    return;
                                }
                            };
                            GasUpdateRequest gasUpdateRequest = new GasUpdateRequest(Double.toString(targetLatitude), Double.toString(targetLongitude), responseListener, errorListener);
                            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                            queue.add(gasUpdateRequest);
                        }
                    });
                    builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Toast.makeText(getApplicationContext(),"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.show();
                }
            }
        });

        rp_btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                Intent intent4 = new Intent(getApplicationContext(), ReportActivity.class);
                try{
                    intent4.putExtra("cur_latitude",currentPoint.getLatitude());
                    Log.d("type", String.valueOf(currentPoint.getLatitude()));
                    intent4.putExtra("cur_longitude",currentPoint.getLongitude());
                    startActivity(intent4);
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(),"현재위치를 인식할 때까지 기다려주세요.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(arr != null){
            tmapview.removeAllMarkerItem();
        }
        GetData task = new GetData();
        task.execute("http://ec2-18-216-239-216.us-east-2.compute.amazonaws.com/ReportPointsRequest.php");
        GetGasData gasTask = new GetGasData();
        gasTask.execute("http://ec2-18-216-239-216.us-east-2.compute.amazonaws.com/GasValueRequest.php");
        //나중에 센서 위치 가져오고 지도에 표시하는 태스크 execute하는 코드 넣으면 됨.
    }

    private class GetData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            //mTextViewResult.setText(result);
            Log.d("report", "response  - " + result);

            if (result == null){
                //mTextViewResult.setText(errorString);
            }
            else {

                mJsonString = result;
                showResult();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];


            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.connect();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d("report", "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString().trim();


            } catch (Exception e) {

                Log.d("report", "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }


    private class GetGasData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Please Wait", null, true, true);
        }



        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            //mTextViewResult.setText(result);
            Log.d("gas value", "response  - " + result);

            if (result == null){
                //mTextViewResult.setText(errorString);
            }
            else {

                mJsonString = result;
                gasShowResult();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];


            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.connect();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d("gas value", "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString().trim();


            } catch (Exception e) {

                Log.d("gas value", "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }


    private void showResult(){
        try {
            Log.d("TAG", "showresult시작");
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++){
                JSONObject item = jsonArray.getJSONObject(i);

                double latitude = Double.parseDouble(item.getString("latitude"));
                double longitude = Double.parseDouble(item.getString("longitude"));
                int state = Integer.parseInt(item.getString("state"));
                String category = item.getString("category");
                Log.d("TAG", "latitude: " + latitude);
                if(state == 0){ //처리중
                    TMapPoint reportPoint = new TMapPoint(latitude, longitude);
                    TMapMarkerItem markerItem = new TMapMarkerItem();
                    markerItem.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                    markerItem.setTMapPoint(reportPoint); // 마커의 좌표 지정
                    markerItem.setCanShowCallout(true);
                    markerItem.setCalloutTitle("처리중");
                    markerItem.setCalloutSubTitle("Hello. LBC World!");

                    Bitmap markerIcon = BitmapFactory.decodeResource(getResources(), R.drawable.red_marker);
                    markerItem.setIcon(markerIcon);

                    markerItem.setCalloutRightButtonImage(markerIcon);

                    tmapview.addMarkerItem("nreport"+i, markerItem); // 지도에 마커 추가 not solved
                    arr.add(markerItem);


                }
                else{ //해결됨
                    TMapPoint reportPoint = new TMapPoint(latitude, longitude);
                    TMapMarkerItem markerItem = new TMapMarkerItem();
                    markerItem.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                    markerItem.setTMapPoint(reportPoint); // 마커의 좌표 지정
                    markerItem.setCanShowCallout(true);
                    markerItem.setCalloutTitle("해결됨");
                    markerItem.setCalloutSubTitle("Hello. LBC World!");
                    Bitmap markerIcon = BitmapFactory.decodeResource(getResources(), R.drawable.blue_marker);
                    markerItem.setIcon(markerIcon);
                    tmapview.addMarkerItem("sreport"+i, markerItem); // 지도에 마커 추가 solved
                    arr.add(markerItem);
                }
            }

        } catch (JSONException e) {

            Log.d("report", "showResult : ", e);
        }

    }

    private void gasShowResult(){
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

                String id = item.getString("id");
                double latitude = Double.parseDouble(item.getString("latitude")); //가스센서의 위치
                double longitude = Double.parseDouble(item.getString("longitude"));
                int state = Integer.parseInt(item.getString("state"));

                if(state == 0){ //아무것도 감지되지 않음
                    TMapPoint reportPoint = new TMapPoint(latitude, longitude);
                    TMapMarkerItem markerItem = new TMapMarkerItem();
                    markerItem.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                    markerItem.setTMapPoint(reportPoint); // 마커의 좌표 지정
                    markerItem.setCanShowCallout(true);
                    markerItem.setCalloutTitle("해결됨");
                    markerItem.setCalloutSubTitle("Hello. LBC World!");
                    Bitmap markerIcon = BitmapFactory.decodeResource(getResources(), R.drawable.gas_safe);
                    markerItem.setIcon(markerIcon);
                    tmapview.addMarkerItem("ngas"+i, markerItem); // 지도에 마커 추가 not detected
                    arr.add(markerItem);
                }
                else{ //감지됨, 서클표시, 알람
                    TMapPoint reportPoint = new TMapPoint(latitude, longitude);
                    TMapMarkerItem markerItem = new TMapMarkerItem();
                    markerItem.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                    markerItem.setTMapPoint(reportPoint); // 마커의 좌표 지정
                    markerItem.setCanShowCallout(true);
                    markerItem.setCalloutTitle("가스유출 발생");
                    markerItem.setCalloutSubTitle("Hello. LBC World!");
                    Bitmap markerIcon = BitmapFactory.decodeResource(getResources(), R.drawable.gas_danger);
                    markerItem.setIcon(markerIcon);
                    markerItem.setCalloutRightButtonImage(markerIcon);
                    tmapview.addMarkerItem("dgas"+i, markerItem); // 지도에 마커 추가 detected
                    arr.add(markerItem);
                }
            }

        } catch (JSONException e) {

            Log.d("gasValue", "gasShowResult : ", e);
        }

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
}


