package com.example.readox.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.readox.R;
import com.example.readox.databinding.ActivityCategoryBinding;
import com.example.readox.utils.UtilsMethod;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CategoryActivity extends AppCompatActivity {
    //view binding
    private ActivityCategoryBinding binding;
    // firebase auth
    private FirebaseAuth firebaseAuth;
    //progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    //    getSupportActionBar().hide();
        //init auth
        firebaseAuth = FirebaseAuth.getInstance();
        //configure progress bar
        progressDialog=new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        //back btn click handle
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //handle click begin upload category
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }
    private  String category="";
    private void validateData() {
        //get data
        category=binding.categoryET.getText().toString().trim();
        //validate if not empty
        if(TextUtils.isEmpty(category)){
            Toast.makeText(this, R.string.errEnterCategoryTitle, Toast.LENGTH_SHORT).show();
        }else
        {
            addCategoryFirebase();
        }

    }

    private void addCategoryFirebase() {
        //show progress msg
        progressDialog.setMessage("Adding Category ...");
        progressDialog.show();
        //get timestamp
        long timestamp=System.currentTimeMillis();
        //setup info to add in firebase db
        HashMap<String,Object> hashMap= new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("category",""+category);
        hashMap.put("timestamp",timestamp);
        hashMap.put("uid",""+firebaseAuth.getUid());
        //add to firebase db , root Categories>CategoryID>categoryinfo
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    //category added successfully
                        progressDialog.dismiss();
                        Toast.makeText(CategoryActivity.this, R.string.msgCategoryUploadSuccess, Toast.LENGTH_SHORT).show();
                        UtilsMethod.sendNotificationNewCategory(CategoryActivity.this,category);
                        UtilsMethod.newNotification();
                        binding.categoryET.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //category add failed
                        progressDialog.dismiss();
                        Toast.makeText(CategoryActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }
}