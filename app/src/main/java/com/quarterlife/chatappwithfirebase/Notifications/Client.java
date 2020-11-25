package com.quarterlife.chatappwithfirebase.Notifications;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Client {
    private static Retrofit retrofit = null;

    public static Retrofit getClient(String url){
        if(retrofit == null){ // 如果 retrofit 為 null

            // 創建 Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit; // 返回 retrofit
    }
}