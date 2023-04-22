package com.example.readox.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.readox.R;
import com.example.readox.databinding.ActivityLoginBinding;
import com.example.readox.utils.CustomDialogs;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    //view binding
    private ActivityLoginBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    CustomDialogs customDialogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
       // getSupportActionBar().hide();
        customDialogs = new CustomDialogs(this);
        //init firebase auth
        firebaseAuth=FirebaseAuth.getInstance();
        //setup progress dialog
        progressDialog=new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);


        //handle no account sign up text view
        binding.noAccountTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });
        //handle login button click
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
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
    private String email="",password="";
    private void validateData() {

        //get data
        email=binding.emailET.getText().toString().trim();
        password=binding.passwordET.getText().toString().trim();
        //validate data
         if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Invalid Email Format...",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Enter Password...", Toast.LENGTH_SHORT).show();
        }else{
             //data validation done ,begin login
             loginUser();
         }
    }

    private void loginUser() {
        progressDialog.setMessage("Logging in...");
        progressDialog.show();
        //login user
        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                   //login success, now check if user is user or admin
                        checkUser();
                        
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //login failed
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void checkUser() {
        progressDialog.setMessage("Checking User...");
        //now check if user is user or admin from realtime db
        //get current user
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        //check in db
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                    //get user type
                        String userType=""+snapshot.child("userType").getValue();
                    //check user type
                        if(userType.equals("user")){
                        //this is user, open user dashboard activity
                            startActivity(new Intent(LoginActivity.this,DashboardUserActivity.class));
                            finish();
                        }else if(userType.equals("admin")) {
                            //this is admin, open admin dashboard activity
                            startActivity(new Intent(LoginActivity.this,DashboardAdminActivity.class));
                            finish();
                        }
                        }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}