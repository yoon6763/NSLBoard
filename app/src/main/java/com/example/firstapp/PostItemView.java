package com.example.firstapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

// MainActivity에서 게시글 목록(listview)를 띄우기 위한 class

public class PostItemView extends LinearLayout {

    TextView tv_title,tv_id,tv_count;

    public PostItemView(Context context)
    {
        super(context);
        init(context);
    }

    public PostItemView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public void init(Context context)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.activity_post_item_view,this,true);

        tv_title = findViewById(R.id.tv_title);
        tv_count = findViewById(R.id.tv_viewcnt);
        tv_id = findViewById(R.id.tv_id);
    }

    public void setName(String name) {tv_id.setText(name);}
    public void setTitle(String title) {tv_title.setText(title);}
    public void setCount(String count) {tv_count.setText(count);}
}