package com.example.firstapp;

import java.net.MalformedURLException;
import java.net.URL;

public class ServerUrl {
    String strurl;

    public ServerUrl()
    {
        String ip = "14.42.62.246";
        strurl = "http://"+ip+":8080/IOTtestproject/";
    }

    public URL getBoard()
    {
        try {
            URL url = new URL(strurl + "Board.jsp");
            return url;
        } catch (MalformedURLException e) { e.printStackTrace(); }
        return null;
    }




}
