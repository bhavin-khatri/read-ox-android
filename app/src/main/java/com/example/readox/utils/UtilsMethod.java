package com.example.readox.utils;

import static com.example.readox.utils.RetrofitClient.BASE_URL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonElement;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UtilsMethod {

    public static NotificationHandler notificationHandler;

    public static String TAG ="Utils";

    public static boolean checkInternetConnection(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);


        if (conMgr.getActiveNetworkInfo()!=null && conMgr.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean m9678c() {
        return Build.VERSION.SDK_INT >= 29;
    }
    public static void openWifiManagerNew(Context context) {
        if (context != null) {
            @SuppressLint("WrongConstant") WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService("wifi");
            if (!wifiManager.isWifiEnabled()) {
                if (m9678c()) {
                    try {
                        context.startActivity(new Intent("android.settings.panel.action.WIFI").addFlags(268435456));
                    } catch (Exception e) {
                        try {
                            final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.wifi.WifiSettings");
                            context.startActivity(new Intent("com.android.settings.wifi.WifiSettings").setComponent(cn));
                        } catch (Exception e1) {
                            e.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                } else {
                    try {
                        context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    } catch (NullPointerException | SecurityException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
    }

    public static void sendNotificationNewCategory(Activity context,String categoryName){
        String title = categoryName + " Added";
        String description = "You Added New Category";
        notificationHandler = new NotificationHandler(context);
        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(description)) {
            Notification.Builder nb = notificationHandler.
                    getAndroidChannelNotification(title, description);
            notificationHandler.getManager().notify(107, nb.build());
        }
    }

    public static void sendNotificationNewBook(Activity context,String bookName){
        String title = bookName + " Added";
        String description = "You Added New Book";
        notificationHandler = new NotificationHandler(context);
        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(description)) {
            Notification.Builder nb = notificationHandler.
                    getAndroidChannelNotification(title, description);
            notificationHandler.getManager().notify(108, nb.build());
        }
    }

    public static void newNotification(){
        SendNotificationModel sendNotificationModel = new SendNotificationModel("check now", "Category Added");
        RequestNotificaton requestNotificaton = new RequestNotificaton();
        requestNotificaton.setSendNotificationModel(sendNotificationModel);
        //token is id , whom you want to send notification ,
        requestNotificaton.setToken("AAAAfvCq4Yk:APA91bETyXsASScvSQlCBtfgn9bpQwSyHVmiASsO6breyiglVKA6OQzsh7VSfb9gFenkjUzllPTMjNfL8YHFURsQc7-PdfwDV2V23neOH2SprLf3XzbeENIeJKfucgHvh0_1qExtggVO");
        OkHttpClient okHttpClient = new OkHttpClient();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiInterface a = retrofit.create(ApiInterface.class);
        Call<RequestNotificaton> call = a.sendChatNotification(requestNotificaton);

        call.enqueue(new Callback<RequestNotificaton>() {
            @Override
            public void onResponse(Call<RequestNotificaton> call, Response<RequestNotificaton> response) {
                Log.d(TAG, "onResponse: ");
            }

            @Override
            public void onFailure(Call<RequestNotificaton> call, Throwable t) {
                Log.d(TAG, "onFailure: ");
            }
        });

    }
}
