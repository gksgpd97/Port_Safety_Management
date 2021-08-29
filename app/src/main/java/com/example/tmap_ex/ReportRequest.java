package com.example.tmap_ex;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class ReportRequest extends StringRequest {
    final static private String URL = "";
    private Map<String, String> parameters;

    public ReportRequest(String report_category, String report_latitude, String report_longitude, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST, URL, listener, errorListener);
        parameters = new HashMap<>();
        parameters.put("report_category", report_category);
        parameters.put("report_latitude", report_latitude);
        parameters.put("report_longitude", report_longitude);
    }

    @Override
    public Map<String, String> getParams() {
        return parameters;
    }
}
