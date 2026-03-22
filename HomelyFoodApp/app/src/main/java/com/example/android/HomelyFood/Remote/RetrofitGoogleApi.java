package com.example.android.HomelyFood.Remote;


import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitGoogleApi {
    private static Retrofit retrofit1=null;

    public static Retrofit getClient(String baseUrl){
        if(retrofit1==null){
            retrofit1=new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit1;
    }
    private static Retrofit retrofit2=null;
    public static Retrofit getGoogleClient(String baseUrl){
        if(retrofit2==null){
            retrofit2=new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofit2;
    }

}

