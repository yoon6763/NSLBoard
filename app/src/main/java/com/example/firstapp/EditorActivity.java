package com.example.firstapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Random;

//글쓰기 버튼을 눌렀을때 뜨는 액티비티, 글쓰기 에디터
public class EditorActivity extends AppCompatActivity {
    EditText et_title, et_contents;
    ServerUrl serverUrl;

    //이미지 관련 변수
    String img_path = new String();
    String img_name = null;
    String imageName = null;
    Bitmap image_bitmap = null;
    Bitmap image_bitmap_copy = null;
    ImageView testimage;

    // 이미지 선택 호출넘버
    // 변수의 값과는 상관 없이 이미지 선택할때의 호출번호와 onActivityResult의 호출번호가 같기만 하면 됨
    int REQ_CODE_SELECT_IMAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //초기화
        serverUrl = new ServerUrl();
        testimage = findViewById(R.id.testimage);
        EditText et_title = findViewById(R.id.et_title);
        EditText et_contents = findViewById(R.id.et_contents);
        Intent intent = getIntent();


        //뭔지 모르는데 멀티파트 오류 안나려면 있어야 한다고 함
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog().build());


        // 글내용에서 수정 버튼 눌렀을 경우 isEdit은 true, 그냥 메인화면에서 글쓰기 버튼 눌렀을 경우 false
        // 글 내용에서 수정으로 눌러서 editor화면을 열었을 때(true) 글 내용의 타이틀과 내용을 받아옴
        if (intent.getExtras().getBoolean("isEdit"))
        {
            et_contents.setText(intent.getExtras().getString("Contents"));
            et_title.setText(intent.getExtras().getString("Title"));
        }

        //이미지 삽입 버튼
        findViewById(R.id.bt_addimg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 이미지 선택하는 액티비티를 띄움 (안드로이드 스튜디오 자체 내장되있는 액티비티)
                // 이미지 선택이 끝나면 onActivityResult 로 결과값이 전달된다. 그냥 아래 4줄은 형식이라 생각하면 편함.
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
            }
        });

        //취소버튼 클릭
        findViewById(R.id.bt_cancel).setOnClickListener(new View.OnClickListener() {public void onClick(View view) { finish(); }});

        //등록 버튼 클릭
        findViewById(R.id.bt_upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = et_title.getText().toString();
                String contents = et_contents.getText().toString();
                String userID = intent.getExtras().getString("userID");

                if (title.length() > 20) { // 제목길이가 20글자 넘는지
                    Toast.makeText(getApplicationContext(), "제목은 최대 20자 까지 입력 가능합니다", Toast.LENGTH_SHORT).show();
                    return;
                }



                // 게시글 수정 - 이미지 없음, 게시글 수정 - 이미지 있음, 글 첫 등록 - 이미지 있음, 글 첫 등록 - 이미지 없음 으로 나뉘어짐
                // if문으로 어캐 통합할 수 있을거 같긴 한데 버그 터질까봐 수정 못하겠음 ㅎㅎ;


                //게시글 - 수정, isEdit이 true
                if (intent.getExtras().getBoolean("isEdit")) {
                    Log.d("수정 / 등록","수정");

                    String num = intent.getExtras().getString("PostIndex");

                    //이미지 없음
                    if(image_bitmap == null) {
                        class UpdateNoPhoto extends AsyncTask {
                            //String filePath = getRealPathFromURI(uri);
                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                            }

                            String lineEnd = "\r\n";
                            String twoHyphens = "--";
                            String boundary = "*****";

                            String str = "";

                            @Override
                            protected Object doInBackground(Object[] objects) { // 스레드의 주 몸체? 부분

                                try {
                                    URL connectUrl = serverUrl.getBoard();

                                    // HttpURLConnection 통신
                                    HttpURLConnection conn = (HttpURLConnection) connectUrl.openConnection();
                                    conn.setDoInput(true);
                                    conn.setDoOutput(true);
                                    conn.setUseCaches(false);
                                    conn.setRequestMethod("POST");
                                    conn.setRequestProperty("Connection", "Keep-Alive");
                                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                                    // write data
                                    DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                                    dos.writeBytes(twoHyphens + boundary + lineEnd);

                                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                                    dos.writeBytes("Content-Disposition: form-data; name=\"type\"" + lineEnd);
                                    dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                                    dos.writeBytes(lineEnd);
                                    dos.writeBytes("noimage_Update"+ lineEnd);

                                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                                    dos.writeBytes("Content-Disposition: form-data; name=\"contents\"" + lineEnd);
                                    dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                                    dos.writeBytes(lineEnd);
                                    dos.writeUTF(contents);
                                    dos.writeBytes(lineEnd);

                                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                                    dos.writeBytes("Content-Disposition: form-data; name=\"id\"" + lineEnd);
                                    dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                                    dos.writeBytes(lineEnd);
                                    dos.writeBytes(userID + lineEnd);

                                    //num값만 추가

                                    String num = intent.getExtras().getString("PostIndex");

                                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                                    dos.writeBytes("Content-Disposition: form-data; name=\"num\"" + lineEnd);
                                    dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                                    dos.writeBytes(lineEnd);
                                    dos.writeBytes(num+ lineEnd);

                                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                                    dos.writeBytes("Content-Disposition: form-data; name=\"title\"" + lineEnd);
                                    dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                                    dos.writeBytes(lineEnd);
                                    dos.writeUTF(title);
                                    dos.writeBytes(lineEnd);

                                    dos.writeBytes("\r\n--" + boundary + "--\r\n");
                                    dos.flush();

                                    BufferedReader rd = null;
                                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                                    String line = "";

                                    while ((line = rd.readLine()) != null) {
                                        str = str + line;
                                        //주의 : str = str + line 으로 받을 경우 앞의 쓰레기값까지 싹 다 받음
                                        //어차피 return 되는 jsonarray는 한줄로 싹다 출력되기에 마지막줄만 받으면 됨
                                    }
                                } catch (Exception e) { e.printStackTrace(); }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Object o) {
                                super.onPostExecute(o);
                                finish();
                            }
                        }
                        UpdateNoPhoto openPost = new UpdateNoPhoto();
                        openPost.execute();
                    }

                    //이미지 있음
                    else
                    {
                        class UpdatePost extends AsyncTask {
                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                            }

                            String str = "";
                            String lineEnd = "\r\n";
                            String twoHyphens = "--";
                            String boundary = "*****";

                            @Override
                            protected Object doInBackground(Object[] objects) { // 스레드의 주 몸체? 부분

                                try {
                                    Log.d("이미지 유무","있음");

                                    File file = new File(img_path);
                                    FileInputStream mFileInputStream = new FileInputStream(file);
                                    URL connectUrl = serverUrl.getBoard();

                                    // HttpURLConnection 통신
                                    HttpURLConnection conn = (HttpURLConnection) connectUrl.openConnection();
                                    conn.setDoInput(true);
                                    conn.setDoOutput(true);
                                    conn.setUseCaches(false);
                                    conn.setRequestMethod("POST");
                                    conn.setRequestProperty("Connection", "Keep-Alive");
                                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                                    // write data
                                    DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                                    dos.writeBytes(twoHyphens + boundary + lineEnd);

                                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                                    dos.writeBytes("Content-Disposition: form-data; name=\"type\"" + lineEnd);
                                    dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                                    dos.writeBytes(lineEnd);
                                    dos.writeBytes(URLEncoder.encode("board_Update","UTF-8") + lineEnd);

                                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                                    dos.writeBytes("Content-Disposition: form-data; name=\"title\"" + lineEnd);
                                    dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                                    dos.writeBytes(lineEnd);
                                    dos.writeBytes(URLEncoder.encode(title,"UTF-8") + lineEnd);

                                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                                    dos.writeBytes("Content-Disposition: form-data; name=\"contents\"" + lineEnd);
                                    dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                                    dos.writeBytes(lineEnd);
                                    dos.writeBytes(URLEncoder.encode(contents,"UTF-8") + lineEnd);

                                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                                    dos.writeBytes("Content-Disposition: form-data; name=\"id\"" + lineEnd);
                                    dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                                    dos.writeBytes(lineEnd);
                                    dos.writeBytes(URLEncoder.encode(userID,"UTF-8") + lineEnd);

                                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                                    dos.writeBytes("Content-Disposition: form-data; name=\"filename\"" + lineEnd);
                                    dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                                    dos.writeBytes(lineEnd);
                                    dos.writeBytes(URLEncoder.encode("filename1","UTF-8") + lineEnd);

                                    //파일의 이름 지정
                                    //서버에 동일한 파일명이 있을 경우 업로드가 되지 않는 현상이 발생하여 Random 메소드를 사용하여 랜덤하게 파일명 생성
                                    Random rd = new Random();
                                    int tempint = rd.nextInt(999999999);
                                    String tempstr = Integer.toString(tempint);

                                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                                    dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + tempstr+".jpg" + "\"" + lineEnd);
                                    dos.writeBytes(("Content-Type: " + URLConnection.guessContentTypeFromName(file.getName())) + lineEnd);
                                    dos.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
                                    dos.writeBytes(lineEnd);

                                    int bytesAvailable = mFileInputStream.available();
                                    int maxBufferSize = 1024;//?1024
                                    int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                                    byte[] buffer = new byte[bufferSize];
                                    int bytesRead = mFileInputStream.read(buffer, 0, bufferSize);

                                    // read image
                                    while (bytesRead > 0) {
                                        dos.write(buffer, 0, bufferSize);
                                        bytesAvailable = mFileInputStream.available();
                                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                                        bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
                                    }

                                    dos.writeBytes(lineEnd);
                                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                                    // close streams
                                    mFileInputStream.close();

                                    dos.flush();
                                    // finish upload...

                                    // get response
                                    InputStream is = conn.getInputStream();

                                    StringBuffer b = new StringBuffer();
                                    for (int ch = 0; (ch = is.read()) != -1; ) {
                                        b.append((char) ch);
                                    }
                                    is.close();

                                } catch (Exception e) { e.printStackTrace(); }
                                return null;

                            }

                            @Override
                            protected void onPostExecute(Object o) {
                                super.onPostExecute(o);
                                finish();
                            }
                        }
                        UpdatePost openPost = new UpdatePost();
                        openPost.execute();
                    }
                }


                // 메인액티비티 - 글쓰기
                else {
                    //사진 없을때

                    Log.d("수정 / 등록","첫 등록");
                    if (image_bitmap == null) {
                        class UploadPostNoPhoto extends AsyncTask {
                            //String filePath = getRealPathFromURI(uri);
                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                            }

                            String lineEnd = "\r\n";
                            String twoHyphens = "--";
                            String boundary = "*****";

                            String str = "";

                            @Override
                            protected Object doInBackground(Object[] objects) { // 스레드의 주 몸체? 부분

                                try {
                                    Log.d("이미지 유무","없음");
                                    URL connectUrl = serverUrl.getBoard();

                                    // HttpURLConnection 통신
                                    HttpURLConnection conn = (HttpURLConnection) connectUrl.openConnection();
                                    conn.setDoInput(true);
                                    conn.setDoOutput(true);
                                    conn.setUseCaches(false);
                                    conn.setRequestMethod("POST");
                                    conn.setRequestProperty("Connection", "Keep-Alive");
                                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                                    // write data
                                    DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                                    dos.writeBytes(twoHyphens + boundary + lineEnd);

                                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                                    dos.writeBytes("Content-Disposition: form-data; name=\"type\"" + lineEnd);
                                    dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                                    dos.writeBytes(lineEnd);
                                    dos.writeBytes("noimage_Add"); // writeUTF 를 쓰면 에러나는듯함
                                    dos.writeBytes(lineEnd);

                                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                                    dos.writeBytes("Content-Disposition: form-data; name=\"contents\"" + lineEnd);
                                    dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                                    dos.writeBytes(lineEnd);
                                    dos.writeUTF(contents);
                                    dos.writeBytes(lineEnd);

                                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                                    dos.writeBytes("Content-Disposition: form-data; name=\"id\"" + lineEnd);
                                    dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                                    dos.writeBytes(lineEnd);
                                    dos.writeBytes(userID);
                                    dos.writeBytes(lineEnd);

                                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                                    dos.writeBytes("Content-Disposition: form-data; name=\"title\"" + lineEnd);
                                    dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                                    dos.writeBytes(lineEnd);
                                    dos.writeUTF(title);
                                    dos.writeBytes(lineEnd);

                                    dos.writeBytes("\r\n--" + boundary + "--\r\n");
                                    dos.flush();

                                    BufferedReader rd = null;
                                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                                    String line = "";

                                    while ((line = rd.readLine()) != null) {
                                        str = str + line;
                                        //주의 : str = str + line 으로 받을 경우 앞의 쓰레기값까지 싹 다 받음
                                        //어차피 return 되는 jsonarray는 한줄로 싹다 출력되기에 마지막줄만 받으면 됨
                                    }
                                    Log.d("str",str);

                                } catch (Exception e) { e.printStackTrace(); }

                                return null;
                            }

                            @Override
                            protected void onPostExecute(Object o) {
                                super.onPostExecute(o);
                                finish();
                            }
                        }
                        UploadPostNoPhoto openPost = new UploadPostNoPhoto();
                        openPost.execute();
                    }

                    //사진 있을때
                    else {
                        class UploadPost extends AsyncTask {
                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                            }

                            String str = "";
                            String lineEnd = "\r\n";
                            String twoHyphens = "--";
                            String boundary = "*****";

                            @Override
                            protected Object doInBackground(Object[] objects) { // 스레드의 주 몸체? 부분

                                try {
                                    Log.d("이미지 유무","있음");

                                    File file = new File(img_path);
                                    FileInputStream mFileInputStream = new FileInputStream(file);
                                    URL connectUrl = serverUrl.getBoard();

                                    // HttpURLConnection 통신
                                    HttpURLConnection conn = (HttpURLConnection) connectUrl.openConnection();
                                    conn.setDoInput(true);
                                    conn.setDoOutput(true);
                                    conn.setUseCaches(false);
                                    conn.setRequestMethod("POST");
                                    conn.setRequestProperty("Connection", "Keep-Alive");
                                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                                    // write data
                                    DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                                    dos.writeBytes(twoHyphens + boundary + lineEnd);

                                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                                    dos.writeBytes("Content-Disposition: form-data; name=\"type\"" + lineEnd);
                                    dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                                    dos.writeBytes(lineEnd);
                                    dos.writeBytes("board_Add" + lineEnd);

                                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                                    dos.writeBytes("Content-Disposition: form-data; name=\"title\"" + lineEnd);
                                    dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                                    dos.writeBytes(lineEnd);
                                    dos.writeUTF(title);
                                    dos.writeBytes(lineEnd);

                                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                                    dos.writeBytes("Content-Disposition: form-data; name=\"contents\"" + lineEnd);
                                    dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                                    dos.writeBytes(lineEnd);
                                    dos.writeUTF(contents);
                                    dos.writeBytes(lineEnd);

                                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                                    dos.writeBytes("Content-Disposition: form-data; name=\"id\"" + lineEnd);
                                    dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                                    dos.writeBytes(lineEnd);
                                    dos.writeBytes(userID + lineEnd);

                                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                                    dos.writeBytes("Content-Disposition: form-data; name=\"filename\"" + lineEnd);
                                    dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                                    dos.writeBytes(lineEnd);
                                    dos.writeBytes("filename1"+ lineEnd);

                                    //파일의 이름 지정
                                    //서버에 동일한 파일명이 있을 경우 업로드가 되지 않는 현상이 발생하여 Random 메소드를 사용하여 랜덤하게 파일명 생성
                                    Random ran = new Random();
                                    int tempint = ran.nextInt(999999999);
                                    String tempstr = Integer.toString(tempint);

                                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                                    dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + tempstr+".jpg" + "\"" + lineEnd);
                                    dos.writeBytes(("Content-Type: " + URLConnection.guessContentTypeFromName(file.getName())) + lineEnd);
                                    dos.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
                                    dos.writeBytes(lineEnd);

                                    int bytesAvailable = mFileInputStream.available();
                                    int maxBufferSize = 1024;//?1024
                                    int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                                    byte[] buffer = new byte[bufferSize];
                                    int bytesRead = mFileInputStream.read(buffer, 0, bufferSize);

                                    // read image
                                    while (bytesRead > 0) {
                                        dos.write(buffer, 0, bufferSize);
                                        bytesAvailable = mFileInputStream.available();
                                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                                        bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
                                    }

                                    dos.writeBytes(lineEnd);
                                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                                    // close streams
                                    mFileInputStream.close();

                                    dos.flush();
                                    // finish upload...


                                    // get response
                                    InputStream is = conn.getInputStream();

                                    StringBuffer b = new StringBuffer();
                                    for (int ch = 0; (ch = is.read()) != -1; ) {
                                        b.append((char) ch);
                                    }
                                    is.close();

                                } catch (Exception e) { e.printStackTrace(); }
                                return null;

                            }

                            @Override
                            protected void onPostExecute(Object o) {
                                super.onPostExecute(o);
                                finish();
                            }

                        }
                        UploadPost openPost = new UploadPost();
                        openPost.execute();
                    }
                }

            }
        });
    }


    /*
     * 설정한 이미지를 비트맵으로 변환, URI를 얻어 경로값을 반환
     * getImagePathToUri 메소드 이용
     * */

    //이미지 선택 액티비티가 종료되면 이 메소드로 결과값이 반환되는듯함
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == REQ_CODE_SELECT_IMAGE) //이미지 선택 호출번호
        {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    Log.d("path",img_path);

                    img_path = getImagePathToUri(data.getData()); //이미지의 URI를 얻어 경로값으로 반환.
                    //이미지를 비트맵형식으로 반환
                    image_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());

                    //사용자 단말기의 width , height 값 반환
                    int reWidth = (int) (getWindowManager().getDefaultDisplay().getWidth());
                    int reHeight = (int) (getWindowManager().getDefaultDisplay().getHeight());

                    //image_bitmap 으로 받아온 이미지의 사이즈를 임의적으로 조절함. width: 400 , height: 300
                    image_bitmap_copy = Bitmap.createScaledBitmap(image_bitmap, 400, 300, true);
                    ImageView image = (ImageView) findViewById(R.id.testimage);  //이미지를 띄울 위젯 ID값
                    image.setImageBitmap(image_bitmap_copy);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getImagePathToUri(Uri data) {
        //사용자가 선택한 이미지의 정보를 받아옴
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        //이미지의 경로 값
        String imgPath = cursor.getString(column_index);

        //이미지의 이름 값
        String imgName = imgPath.substring(imgPath.lastIndexOf("/") + 1);
        this.imageName = imgName;

        return imgPath;
    }
}