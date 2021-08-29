package com.example.tmap_ex;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPoint;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatsActivity extends AppCompatActivity {
    private Context mContext = null;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private static String mApiKey = "";
    private TMapData tmapdata;
    private TMapPoint tMapPoint;
    private String address;
    private ListView reportListView;
    private ReportItemListAdapter reportItemListAdapter;
    private ArrayList<Report_item_list> list_itemArrayList;
    private ArrayList<Report_item_list> newList_itemArrayList;
    private ArrayList<Report_item_list> todayList_itemArrayList;
    private CalendarView calendarView;
    private TextView noReport;
    String sDate;
    String Today;
    Double latitude;
    Double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats_activity);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("신고통계");
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        navigationView = findViewById(R.id.nav);
        navigationView.setItemIconTintList(null);

        Intent intent = getIntent();

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
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        break;

                    case R.id.menu2:
                        //Toast.makeText(mContext, "신고",Toast.LENGTH_LONG).show();
                        Intent intent1 = new Intent(getApplicationContext(), ReportActivity.class);
                        try{
                            intent1.putExtra("cur_latitude",latitude);
                            Log.d("type", String.valueOf(latitude));
                            intent1.putExtra("cur_longitude",longitude);
                            startActivity(intent1);
                        }catch (Exception e){
                            Toast.makeText(getApplicationContext(),"현재위치를 활성화해주세요.", Toast.LENGTH_LONG).show();
                        }
                        break;

                    case R.id.menu3:
                        break;

                }

                drawerLayout.closeDrawer(navigationView);
                return false;
            }
        });

        mContext =this;

        new BackgroundTask().execute();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tMapPoint = new TMapPoint(37.545955, 126.964676);
        tmapdata = new TMapData();

        // 캘린더뷰
        calendarView = (CalendarView) findViewById(R.id.simple_calendarview);
        noReport = (TextView)findViewById(R.id.noReport);

        long selectedDate = calendarView.getDate();
        Log.d("selected", Long.toString(selectedDate));

        Calendar mCalendar = Calendar.getInstance();
        Date today = mCalendar.getTime();
        SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        Today = dfDate.format(today);
        Log.d("time: ", Today);

        // 리스트뷰
        reportListView = (ListView)findViewById(R.id.report_listView);

        list_itemArrayList = new ArrayList<Report_item_list>();
        todayList_itemArrayList = new ArrayList<Report_item_list>();
        //reportItemListAdapter = new ReportItemListAdapter(StatsActivity.this,list_itemArrayList);
        //reportReclyclerView.setAdapter(reportItemListAdapter);

    }

    // MySQL에서 데이터 받아오기
    class BackgroundTask extends AsyncTask<Void, Void, String> {

        String target;

        @Override
        protected  void onPreExecute() {
            try {
                target = "";
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("printStackTrace","에러");
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                list_itemArrayList.clear();
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("response");
                int count = 0;
                Drawable category_image;
                String report_id;
                String report_category;
                String report_latitude;
                String report_longitude;
                String report_date;
                String report_state;
                String resolved_date;

                while(count < jsonArray.length()) {
                    JSONObject object = jsonArray.getJSONObject(count);
                    category_image = null;
                    report_id = object.getString("report_id");
                    report_category = object.getString("report_category");
                    report_latitude = object.getString("report_latitude");
                    report_longitude = object.getString("report_longitude");
                    report_date = object.getString("report_date");
                    Log.d("report_date:", report_date.substring(0,10));
                    report_state = object.getString("report_state");
                    resolved_date = object.getString("resolved_date");

                    // category_image 지정

                    // report_category 지정
                    switch (report_category) {
                        case "1":
                            report_category = "근로자 추락";
                            category_image = getResources().getDrawable(R.drawable.ic_1);
                            break;
                        case "2":
                            report_category = "화물 낙하";
                            category_image = getResources().getDrawable(R.drawable.ic_2);
                            break;
                        case "3":
                            report_category = "협착 사고";
                            category_image = getResources().getDrawable(R.drawable.ic_3);
                            break;
                        case "4":
                            report_category = "화재 사고";
                            category_image = getResources().getDrawable(R.drawable.ic_4);
                            break;
                        case "5":
                            report_category = "가스누출 사고";
                            category_image = getResources().getDrawable(R.drawable.ic_5);
                            break;
                        case "6":
                            report_category = "교통 사고";
                            category_image = getResources().getDrawable(R.drawable.ic_6);
                            break;
                        case "7":
                            report_category = "기타";
                            category_image = getResources().getDrawable(R.drawable.ic_7);
                            break;
                    }

                    // report_state 지정
                    switch (report_state) {
                        case "0":
                            report_state = "처리중";
                            break;
                        case "1":
                            report_state = "해결됨";
                            break;
                    }

                    final Geocoder geocoder = new Geocoder(StatsActivity.this);
                    try{
                        List<Address> mResultList = geocoder.getFromLocation(Double.parseDouble(report_latitude), Double.parseDouble(report_longitude), 1);
                        address = mResultList.get(0).getAddressLine(0);
                        if(address.substring(0,4).equals("대한민국")){
                            address = address.substring(5,address.length());
                        }
                        Log.d("georesult: ",mResultList.get(0).getAddressLine(0));
                    }catch (Exception e){
                        e.printStackTrace();
                        Log.d("georesult: ","fail");
                    }

                    Report_item_list report_item_list = new Report_item_list(category_image, report_category, address, report_date, report_state,resolved_date);
                    list_itemArrayList.add(report_item_list);
                    if(Today.equals(report_date.substring(0,10))){
                        todayList_itemArrayList.add(report_item_list);
                    }
                    count++;
                }
                Log.d("size:", String.valueOf(list_itemArrayList.size()));
                // reportItemListAdapter = new ReportItemListAdapter(getApplicationContext(),list_itemArrayList);
                reportItemListAdapter = new ReportItemListAdapter(StatsActivity.this, todayList_itemArrayList);
                reportListView.setAdapter(reportItemListAdapter);
                //reportItemListAdapter.notifyDataSetChanged();
                calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                    @Override
                    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                        month+=1;
                        String tStringMonth = Integer.toString(month);
                        String StringMonth = null;
                        String tSdayOfMonth = Integer.toString(dayOfMonth);
                        String StringdayOfMonth = null;
                        if (month>=1 && month<=9){
                            StringMonth = "0"+tStringMonth;
                        }
                        else{
                            StringMonth = tStringMonth;
                        }
                        if (dayOfMonth>=1 && dayOfMonth<=9){
                            StringdayOfMonth = "0"+tSdayOfMonth;
                        }else{
                            StringdayOfMonth = tSdayOfMonth;
                        }
                        sDate = year+"-"+StringMonth+"-"+StringdayOfMonth;
                        //Toast.makeText(getApplicationContext(), year+"-"+StringMonth+"-"+StringdayOfMonth, Toast.LENGTH_SHORT).show();

                        int count = 0;
                        newList_itemArrayList = new ArrayList<Report_item_list>();
                        if (newList_itemArrayList != null){
                            newList_itemArrayList.clear();
                        }
                        while(count < list_itemArrayList.size()){
                            Log.d("redate:", list_itemArrayList.get(count).getReport_date());
                            if(sDate.equals(list_itemArrayList.get(count).getReport_date().substring(0,10))){
                                Report_item_list newReport_item_list = list_itemArrayList.get(count);
                                newList_itemArrayList.add(newReport_item_list);
                            }
                            count++;
                        }
                        reportItemListAdapter = new ReportItemListAdapter(StatsActivity.this, newList_itemArrayList);
                        reportListView.setAdapter(reportItemListAdapter);
                        //noReport.setText("신고된 사고가 없습니다.");
                        if(newList_itemArrayList.size()==0) {
                            noReport.setText("신고된 사고가 없습니다.");
                        }else{
                            noReport.setText("");
                        }
                    }
                });

                if(todayList_itemArrayList==null){
                    noReport.setText("신고된 사고가 없습니다.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("printStackTrace","에러");
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
                Log.d("printStackTrace","에러");            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
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
