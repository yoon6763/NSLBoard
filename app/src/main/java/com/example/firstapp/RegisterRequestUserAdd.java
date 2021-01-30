package com.example.firstapp;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

//회원가입 정보 등록 리퀘스트
public class RegisterRequestUserAdd extends StringRequest {

    //서버 URL 설정(php 파일 연동)
    final static private String URL = "http://14.38.147.134:8080/IOTtestproject/User.jsp";
    private Map<String, String>parameters; //다른 클래스에서 map이라 한것도 있는데 그냥 똑같은거

    public RegisterRequestUserAdd(String userID, String userPassword, String userName, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        parameters = new HashMap<>();
        parameters.put("id", userID);
        parameters.put("name", userName);
        parameters.put("type","addUser_Add");
    }

    @Override
    protected Map<String, String>getParams() throws AuthFailureError {
        return parameters;
    }
}