package com.example.firstapp;

public class PostItem {

    //리스트뷰에 들어갈 자료들을 담고있는 클래스
    String id;
    String title;
    String count;
    String postIndex;

    public PostItem(String title, String id, String count, String postIndex)
    {
        this.id = id;
        this.count = count;
        this.title = title;
        this.postIndex = postIndex;
    }

    public String getId() {return id;}
    public String getTitle() {return title;}
    public String getCount() {return count;}
    public String getPostIndex() {return postIndex;}

    public String toString()
    {
        return "BoardItem{"+"title = "+title + "name = " +id +"count = "+count+"postIndex = "+postIndex+"}";
    }

}
