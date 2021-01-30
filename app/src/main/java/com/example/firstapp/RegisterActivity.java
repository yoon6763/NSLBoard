package com.example.firstapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

//회원가입 화면 액티비티
public class RegisterActivity extends AppCompatActivity {

    private EditText et_id, et_pass, et_name, et_email;
    private Button btn_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_register );

        //아이디값 찾아주기
        et_id = findViewById( R.id.join_id );
        et_pass = findViewById( R.id.join_pw );
        et_name = findViewById( R.id.join_nickname );
        et_email = findViewById( R.id.join_email);

        //회원가입 버튼 클릭 시 수행
        btn_register = findViewById( R.id.join_button );
        btn_register.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userID = et_id.getText().toString();
                String userPass = et_pass.getText().toString();
                String userName = et_name.getText().toString();
                String userMail = et_email.getText().toString();

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            String str = new String(response);
                            if(str.equals("acountAleadyExist"))
                                Toast.makeText(getApplicationContext(),"이미 존재하는 ID 입니다.",Toast.LENGTH_SHORT).show();

                        } catch (Exception e) { e.printStackTrace(); }
                    }
                };

                //서버로 Volley를 이용해서 요청
                RegisterRequestUserAdd registerRequestUserAdd = new RegisterRequestUserAdd( userID, userPass, userName, responseListener);
                RequestQueue queue = Volley.newRequestQueue( RegisterActivity.this );
                queue.add(registerRequestUserAdd);

                Response.Listener<String> responseListener2 = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            String str = new String(response);
                            Toast.makeText(getApplicationContext(),str,Toast.LENGTH_SHORT).show();

                            if(str.equals("accountCreated"))
                            {
                                Toast.makeText(getApplicationContext(),"회원가입에 성공하셨습니다",Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } catch (Exception e) { e.printStackTrace();}
                    }
                };

                //서버로 Volley를 이용해서 요청
                RegisterRequestJoin registerRequestJoin = new RegisterRequestJoin( userID, userPass, userName, userMail, responseListener2);
                RequestQueue queue1 = Volley.newRequestQueue( RegisterActivity.this );
                queue1.add(registerRequestJoin);
            }
        });
    }
}