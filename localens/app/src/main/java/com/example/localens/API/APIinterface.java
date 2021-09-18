package com.example.localens.API;

import com.example.localens.data_model;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface APIinterface {


    @POST("/insert_data")
    Call<String> insert_data(@Body data_model data);

    @POST("/fetch_data")
    Call<ArrayList<data_model>> fetch_data(@Body data_model data);

    @POST("/fetch_image")
    Call<String> fetch_image(@Body data_model data);
}
