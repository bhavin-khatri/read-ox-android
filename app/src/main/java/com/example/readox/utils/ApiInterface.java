package com.example.readox.utils;

import com.google.gson.JsonElement;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiInterface {
    @Headers({
            "Authorization: key=AAAAfvCq4Yk:APA91bETyXsASScvSQlCBtfgn9bpQwSyHVmiASsO6breyiglVKA6OQzsh7VSfb9gFenkjUzllPTMjNfL8YHFURsQc7-PdfwDV2V23neOH2SprLf3XzbeENIeJKfucgHvh0_1qExtggVO",
            "Content-Type:application/json"
    })
    @POST("fcm/send")
    Call<RequestNotificaton> sendChatNotification(@Body RequestNotificaton requestNotificaton);
}
