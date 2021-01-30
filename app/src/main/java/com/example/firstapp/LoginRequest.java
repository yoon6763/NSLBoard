package com.example.firstapp;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

//로그인 리퀘스트
public class LoginRequest extends StringRequest {

    //서버 URL 설정(php 파일 연동)
    final static private String URL = "http://14.42.62.246:8080/IOTtestproject/Login.jsp";
    private Map<String, String>parameters; //다른 클래스에서 map이라 한것도 있는데 그냥 똑같은거
    //private Map<String, String>parameters;

    public LoginRequest(String userID, String userPassword, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        parameters = new HashMap<>();
        parameters.put("id", userID);
        parameters.put("pwd", userPassword);
        parameters.put("type","login");
    }

    @Override
    protected Map<String, String>getParams() throws AuthFailureError {
        return parameters;
    }
}