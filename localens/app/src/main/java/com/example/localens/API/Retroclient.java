package com.example.localens.API;

import retrofit2.Retrofit;

public class Retroclient {
    private static String url="https://localens.rajurastogi.repl.co";
    //private static String url="http://192.168.43.83:5000";
    public static APIinterface retroinit()
    {
        Retrofit retrofit= MyRetrofit.getRetrofit(url);
        APIinterface apIinterface = retrofit.create(APIinterface.class);
        return apIinterface;
    }
}
