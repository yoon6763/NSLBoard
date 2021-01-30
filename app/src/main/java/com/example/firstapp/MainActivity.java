package com.example.firstapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;

//로그인 이후 액티비티
public class MainActivity extends AppCompatActivity {

    String id;
    ServerUrl serverUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serverUrl = new ServerUrl();

        // 로그인 화면에서 받아온 ID와 name을 텍스트뷰에 입력
        Intent intent = getIntent();
        TextView tv_id = findViewById(R.id.tv_id);
        TextView tv_name = findViewById(R.id.tv_name);
        id = intent.getExtras().getString("userID"); // 로그인 화면에서 받아온 ID
        String name = intent.getExtras().getString("userName"); // 받아온 name
        tv_id.setText(id);
        tv_name.setText(name);

        //글쓰기 버튼 클릭
        findViewById(R.id.bt_write).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editorIntent = new Intent(getApplication(), EditorActivity.class);
                editorIntent.putExtra("userName", name);
                editorIntent.putExtra("userID", id);
                editorIntent.putExtra("isEdit",false); // 글쓰기로 열었을땐 false, 게시글 수정으로 열었을땐 true를 줄것임.
                startActivity(editorIntent);
            }
        });

        // 게시글 리스트 불러오기
        GetList glt = new GetList();
        glt.execute();

        //새로고침 레이아웃 (당겨서(swipe) 새로고침)
        SwipeRefreshLayout refreshLayout = findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setColorSchemeResources(android.R.color.holo_blue_light); // 새로고침 아이콘 색상 변경
                GetList glt = new GetList();
                glt.execute();
                refreshLayout.setRefreshing(false); //새로고침 아이콘 제거, 안해주면 새로고침중 아이콘이 안사라짐
            }
        });

        // 게시글 리스트 새고로침 (이미지 파일)
        findViewById(R.id.img_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GetList glt = new GetList();
                glt.execute();
            }
        });


        //신기능 스레드 자동새로고침
        class AutoRefresh extends Thread
        {
            public AutoRefresh() {}
            public void run()
            {
                while(true) {
                    try {
                        GetList glt = new GetList();
                        glt.execute();
                        sleep(3000); // 3초마다 자동 새로고침
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
        }
        AutoRefresh autoRefresh = new AutoRefresh();
        autoRefresh.start();
    }

    // 게시글 리스트를 받아오는 메소드
    class GetList extends AsyncTask{
        String str = "";

        ListView listview = (ListView) findViewById(R.id.listview);
        PostAdapter adapter = new PostAdapter();

        @Override
        protected void onPreExecute() {}

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                //MultiPart 사용
                String lineEnd = "\r\n";
                String twoHyphens = "--";

                URL url = serverUrl.getBoard();
                String boundary = "SpecificString";
                URLConnection con = url.openConnection();
                con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                con.setDoOutput(true);

                DataOutputStream dos = new DataOutputStream(con.getOutputStream());

                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"type\"\r\n\r\n" + "board_List"); // type 부분이 POST (KEY), board_List 부분이 value 라 생각하면 편할듯

                dos.writeBytes("\r\n--" + boundary + "--\r\n");
                dos.flush();

                BufferedReader rd = null;
                rd = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

                String line = "";

                str = rd.readLine();
                while ((line = rd.readLine()) != null) {
                    str = line;
                    //주의 : str = str + line 으로 받을 경우 앞의 쓰레기값까지 싹 다 받음
                    //어차피 return 되는 jsonarray는 한줄로 싹다 출력되기에 마지막줄만 받으면 됨
                }
                //서버에서 return 해준 jsonarray값이 str에 저장됨

            }catch (Exception e) { e.printStackTrace(); };
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            String[][] strlist;
//            String 구조
//            String[?][0] -> title
//            String[?][1] -> id
//            String[?][2] -> count
//            String[?][3] -> num (일련번호)

            try {
                JSONArray jsonArray = new JSONArray(str); //String값인 str을 이용하여 jsonarray 형태로 만듬
                strlist = new String[jsonArray.length()][4];

                for(int i = 0; i<jsonArray.length(); i++)
                {
                    //JSONObject의 배열 형태가 JSONArray
                    //jsonarray의 i번째 jsonobject를 받아옴
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    //jsonarray의 각 값을 각 strlist에 저장함
                    strlist[i][0] = URLDecoder.decode(jsonObject.getString("title")); // utf-8 타입 디코딩. 나중에 오류가 생길수도 있으니 조심.
                    strlist[i][1] = jsonObject.getString("id");
                    strlist[i][2] = jsonObject.getString("viewcnt");
                    strlist[i][3] = jsonObject.getString("num");
                }

                for(int i = jsonArray.length()-1; i>=0; i--)
                    adapter.addItem(new PostItem(strlist[i][0], strlist[i][1], strlist[i][2],strlist[i][3]));
            } catch (JSONException e) { e.printStackTrace(); }




            //리스트 목록 클릭 이벤트 (게시글 열기)
            listview.setAdapter(adapter);
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    PostItem tempItem = (PostItem) adapter.getItem(i);
                    String tempIndex = tempItem.getPostIndex();

                    String OpenStr = "";

                    class OpenPost extends AsyncTask
                    {
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
                                dos.writeBytes("Content-Disposition: form-data; name=\"type\"\r\n\r\n" + "board_View"); // type 부분이 POST (KEY), board_List 부분이 value 라 생각하면 편할듯

                                dos.writeBytes("\r\n--" + boundary + "\r\n");
                                dos.writeBytes("Content-Disposition: form-data; name=\"num\"\r\n\r\n" + tempIndex);

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
                            }catch (Exception e) { e.printStackTrace(); };
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Object o) {
                            super.onPostExecute(o);
                            try {
                                str = URLDecoder.decode(str,"utf-8");

                                //jsonarray로 return 받았는데 배열형태기 때문에 그냥 0번째 인덱스 꺼내와서 jsonobject 형으로 만듬
                                JSONArray jsonArray = new JSONArray(str);
                                JSONObject jsonObject = jsonArray.getJSONObject(0);

                                //받아온 JSONObject에서 값을 추출하여 저장함
                                String userID = jsonObject.getString("id");
                                String PostIndex = jsonObject.getString("num");
                                String Title = jsonObject.getString("title");
                                String count = jsonObject.getString("viewcnt");
                                String Contents = jsonObject.getString("contents");
                                String date = jsonObject.getString("date");

                                //게시글 띄우기 + 엑스트라로 값들 넘겨주기
                                Intent intent1 = new Intent(MainActivity.this, PostContentsActivity.class);
                                intent1.putExtra("Title", Title);
                                intent1.putExtra("Contents", Contents);
                                intent1.putExtra("count", count);
                                intent1.putExtra("userID", userID);
                                intent1.putExtra("PostIndex", PostIndex);
                                intent1.putExtra("date",date);

                                startActivity(intent1);

                            } catch (Exception e) { e.printStackTrace(); }
                        }
                    }

                    OpenPost openPost = new OpenPost();
                    openPost.execute();
                }
            });
        }
    }

    //listView 구조 클래스
    class PostAdapter extends BaseAdapter{
        ArrayList<PostItem> items = new ArrayList<PostItem>();
        public void addItem(PostItem item) { items.add(item);}
        public int getCount() { return items.size(); }
        public Object getItem(int i) {return items.get(i);}
        public long getItemId(int i) {return i;}
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            PostItemView view1 = new PostItemView(getApplicationContext());
            PostItem item = items.get(i);
            view1.setName(item.getId());
            view1.setCount(item.getCount());
            view1.setTitle(item.getTitle());
            return view1;
        }
    }
}