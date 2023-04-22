package com.example.readox.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.example.readox.R;
import com.example.readox.databinding.ActivityMainBinding;
import com.example.readox.utils.CustomDialogs;
import com.example.readox.utils.NotificationHandler;
import com.example.readox.utils.UtilsMethod;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    CustomDialogs customDialogs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        customDialogs = new CustomDialogs(this);
       // getSupportActionBar().hide();
        //handle login button , start login screen

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
            }
        });


    }

    private void showExitDialog(){
        customDialogs.customExitDialog(this, new CustomDialogs.OnDialogListener( ) {
            @Override
            public void onDialogYes(Dialog dialog) {

            }

            @Override
            public void onDialogNo(Dialog dialog) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        showExitDialog();

    }
}