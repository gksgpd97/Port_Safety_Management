package com.example.tmap_ex;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class LoginRequest extends StringRequest {
    final static private String URL = "";
    private Map<String, String> parameters;

    public LoginRequest(String user_id, String user_pwd, String get_token, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("user_id", user_id);
        parameters.put("user_pwd", user_pwd);
        parameters.put("user_token", get_token);
    }

    @Override
    public Map<String, String> getParams() {
        return parameters;
    }
}
