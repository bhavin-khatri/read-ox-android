package com.example.readox.activities;

import static com.example.readox.utils.UtilsMethod.openWifiManagerNew;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import com.example.readox.R;
import com.example.readox.utils.CustomDialogs;
import com.example.readox.utils.MyApplication;
import com.example.readox.utils.UtilsMethod;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



public class SplashActivity extends AppCompatActivity {
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private CustomDialogs customDialogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        customDialogs = new CustomDialogs(this);
        checkInternet();

    }

    private void checkInternet() {
        if (UtilsMethod.checkInternetConnection(this)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkUser();
                }
            },2000);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    customDialogs.onWifiDisConnectedDialog(SplashActivity.this, new CustomDialogs.OnWifiDisConnectedDialog() {
                        @Override
                        public void onGotoSettings(Dialog dialog) {
                            dialog.dismiss();
                            openWifiManagerNew(SplashActivity.this);
                        }

                        @Override
                        public void onSkip(Dialog dialog) {
                            dialog.dismiss();
                            checkInternet();
                        }
                    });
                }
            },500);
        }
    }

    private void checkUser() {
        //check user if already logged in
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        if(firebaseUser==null)
        {
            //user not logged in
            startActivity(new Intent(SplashActivity.this,MainActivity.class));
            finish();
        }else{
            //user logged in check user type
            //check in db
            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            //get user type
                            String userType=""+snapshot.child("userType").getValue();
                            //check user type
                            if(userType.equals("user")){
                                //this is user, open user dashboard activity
                                startActivity(new Intent(SplashActivity.this,DashboardUserActivity.class));
                                finish();
                            }else if(userType.equals("admin")) {
                                //this is admin, open admin dashboard activity
                                startActivity(new Intent(SplashActivity.this,DashboardAdminActivity.class));
                                finish();
                            }
                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }
}