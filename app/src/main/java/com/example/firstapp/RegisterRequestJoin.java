package com.example.firstapp;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RegisterRequestJoin extends StringRequest {

    //서버 URL 설정(php 파일 연동)
    final static private String URL = "http://14.38.147.134:8080/IOTtestproject/Login.jsp";
    private Map<String, String> parameters; //다른 클래스에서 map이라 한것도 있는데 그냥 똑같은거
    //private Map<String, String>parameters;

    public RegisterRequestJoin(String userID, String userPassword, String userName, String mail, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        parameters = new HashMap<>();
        parameters.put("id", userID);
        parameters.put("pwd", userPassword);
        parameters.put("name", userName);
        parameters.put("mail",mail);
        parameters.put("type","Join");
    }

    @Override
    protected Map<String, String>getParams() throws AuthFailureError {
        return parameters;
    }
}