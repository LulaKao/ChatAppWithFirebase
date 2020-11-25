package com.quarterlife.chatappwithfirebase.Fragments;

import com.quarterlife.chatappwithfirebase.Notifications.MyResponse;
import com.quarterlife.chatappwithfirebase.Notifications.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public abstract class APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAmpaZ6x8:APA91bHkEmYP2e4LVoEcp-zMuUtJ5fHygPdAMv-mx1UoxT7HX-QPwRkcfTY_BCKQMIFbkwWxwGCJdkaZS7dPPz80Jl7UUAYt8PWs5_4PcAvs_7PS78vhHmd5ZOdp0jalUcmouqeXxXZi"
            }
    )

    @POST("fcm/send")
    abstract Call<MyResponse> sendNotification(@Body Sender body);
}
