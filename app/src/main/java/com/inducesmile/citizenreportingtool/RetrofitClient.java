package com.inducesmile.citizenreportingtool;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    /*private static final String BASE_URL = "http://127.0.0.1:8000/myServer/";*/
    /*private static final String BASE_URL = "http://192.168.1.4/myServer/";*/
    private static final String BASE_URL = "http://localhost:8000/myServer/";
    private static RetrofitClient mInstance;
    private Retrofit retrofit;


    public RetrofitClient() {
        this.retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
    }

    public static synchronized RetrofitClient getInstance(){
        if (mInstance == null){
            mInstance = new RetrofitClient();
        }
        return  mInstance;
    }

    public Api getApi(){
        return retrofit.create(Api.class);
    }
}
