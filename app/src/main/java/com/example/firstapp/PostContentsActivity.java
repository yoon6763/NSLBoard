package com.example.firstapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

//리스트뷰에서 게시글을 클릭하면 게시글 내용을 보여줄 액티비티
public class PostContentsActivity extends AppCompatActivity {

    TextView tv_count, tv_name,tv_contents,tv_title;
    ServerUrl serverUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_contents);
        tv_contents = findViewById(R.id.tv_contents);
        tv_count = findViewById(R.id.tv_viewcnt);
        tv_name = findViewById(R.id.tv_name);
        tv_title = findViewById(R.id.tv_title);
        serverUrl = new ServerUrl();

        Intent intent = getIntent();

        //되돌아가기 이미지 클릭
        findViewById(R.id.img_back).setOnClickListener(new View.OnClickListener() {public void onClick(View view) {finish();}});

        //개행문자 제거. ( \n 을 지워서 한줄로 만듦 )
        String gettitle = intent.getExtras().getString("Title");
        gettitle = gettitle.replaceAll(System.getProperty("line.separator")," ");

        //textview들 설정
        init(gettitle,intent.getExtras().getString("count"),intent.getExtras().getString("userID"),intent.getExtras().getString("Contents"));
        String postindex = intent.getExtras().getString("PostIndex");



        //게시글에 띄울 이미지
        ImageView imageView = findViewById(R.id.imageView);

        //서버로부터 이미지 받아오는 클래스
        class GetImageServer extends AsyncTask
        {
            String read = null;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(Object[] objects) {
                try {
                    String lineEnd = "\r\n";
                    String twoHyphens = "--";

                    URL url = serverUrl.getBoard();
                    String boundary = "SpecificString";
                    URLConnection con = url.openConnection();
                    con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                    con.setDoOutput(true);

                    DataOutputStream dos = new DataOutputStream(con.getOutputStream());

                    dos.writeBytes("\r\n--" + boundary + "\r\n");
                    dos.writeBytes("Content-Disposition: form-data; name=\"type\"\r\n\r\n" + "imgShow"); // type 부분이 POST (KEY), board_List 부분이 value 라 생각하면 편할듯

                    dos.writeBytes("\r\n--" + boundary + "\r\n");
                    dos.writeBytes("Content-Disposition: form-data; name=\"num\"\r\n\r\n" + postindex);

                    dos.writeBytes("\r\n--" + boundary + "--\r\n");
                    dos.flush();

                    BufferedReader rd = null;
                    rd = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

                    String line = "";

                    while ((line = rd.readLine()) != null) {
                        read = line;
                    }

                }catch (Exception e) { e.printStackTrace(); };
                return read;
            }
            @Override
            protected void onPostExecute(Object o) { super.onPostExecute(o); }
        }

        String readImage = null;

        //위의 서버로부터 이미지 받아오는 클래스를 실행
        try {
            GetImageServer getImageServer = new GetImageServer();

            // AsyncTask(쓰레드)를 execute(실행, run()과 비슷한 개념) 하고 get을 하면 스레드의 결과값을 받아올 수 있음.
            // 위 GetImageServer 클래스의 doInBackground의 return 값인 read가 반환.
            readImage = String.valueOf(   getImageServer.execute().get()   );

            // 받아온 이미지는 인코딩 되어 있기에 String 형태임

        } catch (Exception e) { e.printStackTrace(); }

        if(readImage != null) {
            Bitmap bitmap = StringToBitmap(readImage); // 인코딩되어 String 형태인 이미지를 디코딩하여 비트맵(그림파일) 형식으로 받아옴
            imageView.setImageBitmap(bitmap); // 비트맵 형식의 이미지를 이미지뷰에 설정함
        }

        // * 참고
        // 원래는 이미지 받아오기도 MainActivity 에서 했었는데
        // putExtra로 전달해 줄 수 있는 크기는 최대 1mb 정도가 한계라고 함
        // 따라서 현재 액티비티에서 이미지를 받아옴







        //삭제 글씨(textview) 클릭
        findViewById(R.id.tv_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(PostContentsActivity.this);
                dlg.setTitle("게시글 삭제");
                dlg.setMessage("게시글을 삭제하시겠습니까?");

                //삭제 - 확인 버튼 클릭
                dlg.setPositiveButton("확인",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                        class DeletePost extends AsyncTask
                        {
                            String str = "";
                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                            }

                            @Override
                            protected Object doInBackground(Object[] objects) { // 스레드의 주 몸체? 부분
                                try {
                                    String lineEnd = "\r\n";
                                    String twoHyphens = "--";

                                    URL url = serverUrl.getBoard();
                                    String boundary = "SpecificString";
                                    URLConnection con = url.openConnection();
                                    con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                                    con.setDoOutput(true);

                                    DataOutputStream dos = new DataOutputStream(con.getOutputStream());

                                    dos.writeBytes("\r\n--" + boundary + "\r\n");
                                    dos.writeBytes("Content-Disposition: form-data; name=\"type\"\r\n\r\n" + "board_Delete");

                                    dos.writeBytes("\r\n--" + boundary + "\r\n");
                                    dos.writeBytes("Content-Disposition: form-data; name=\"num\"\r\n\r\n" + postindex);

                                    dos.writeBytes("\r\n--" + boundary + "\r\n");
                                    dos.writeBytes("Content-Disposition: form-data; name=\"id\"\r\n\r\n" + intent.getExtras().getString("userID"));

                                    dos.writeBytes("\r\n--" + boundary + "--\r\n");
                                    dos.flush();

                                    BufferedReader rd = null;
                                    rd = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

                                    String line = "";

                                    while ((line = rd.readLine()) != null) {
                                        str = line;
                                        //주의 : str = str + line 으로 받을 경우 앞의 쓰레기값까지 싹 다 받음
                                        //어차피 return 되는 jsonarray는 한줄로 싹다 출력되기에 마지막줄만 받으면 됨
                                    }

                                } catch (Exception e) {e.printStackTrace();}
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Object o) {
                                super.onPostExecute(o);
                                if(str.equals("board_Deleted"))
                                    finish();
                                else
                                    Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();
                            }
                        }
                        DeletePost dp = new DeletePost();
                        dp.execute();
                    }
                });

                dlg.setNegativeButton("취소",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                        //토스트 메시지
                        Toast.makeText(PostContentsActivity.this,"취소를 선택하셨습니다.",Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.show();
            }
        });

        //수정 글씨(textview)클릭
        findViewById(R.id.tv_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(PostContentsActivity.this);
                dlg.setTitle("게시글 수정");
                dlg.setMessage("게시글을 수정하시겠습니까?");

                //수정 - 확인 버튼 클릭
                dlg.setPositiveButton("확인",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                        //intentedit은 editoractivity를 열고 전달해줄 인텐트, intent는 mainactivity에서 전달받은 인텐트
                        Intent intentedit = new Intent(getApplication(),EditorActivity.class);
                        intentedit.putExtra("Contents",intent.getExtras().getString("Contents"));
                        intentedit.putExtra("Title",intent.getExtras().getString("Title"));
                        intentedit.putExtra("userID",intent.getExtras().getString("userID"));
                        intentedit.putExtra("PostIndex",intent.getExtras().getString("PostIndex"));
                        intentedit.putExtra("isEdit",true); // 글쓰기로 열었을땐 false, 게시글 수정으로 열었을땐 true를 줄것임.
                        startActivity(intentedit);
                        finish();
                    }
                });

                dlg.setNegativeButton("취소",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                        //토스트 메시지
                        Toast.makeText(PostContentsActivity.this,"취소를 선택하셨습니다.",Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.show();
            }
        });
    }

    public void init(String title, String count, String name, String contents)
    {
        tv_title.setText(title);
        tv_name.setText(name);
        tv_count.setText(count);
        tv_contents.setText(contents);
    }

    //인코딩된 String 형태의 이미지를 Bitmap형태로 만드는 메소드
    public static Bitmap StringToBitmap(String encodedString)
    {
        try {
            byte[] encodeByte = Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte,0,encodeByte.length);
            return bitmap;
        } catch(Exception e) { e.getStackTrace(); }
        return null;
    }
}