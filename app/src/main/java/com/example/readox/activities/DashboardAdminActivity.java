package com.example.readox.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.example.readox.R;
import com.example.readox.adapters.AdapterPdfAdmin;
import com.example.readox.models.ModelCategory;
import com.example.readox.adapters.AdapterCategory;
import com.example.readox.databinding.ActivityDashboardAdminBinding;
import com.example.readox.utils.CustomDialogs;
import com.example.readox.utils.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardAdminActivity extends AppCompatActivity {

    //view binding
    private ActivityDashboardAdminBinding binding;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //array list to store category list
    private ArrayList<ModelCategory> categoryArrayList;
    //adapter
    private AdapterCategory adapterCategory;
    CustomDialogs customDialogs;

    private PreferenceManager preferenceManager;

    private static final String TAG = "Dashboard_Admin";


    public String getThemeName()
    {
        PackageInfo packageInfo;
        try
        {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
            int themeResId = packageInfo.applicationInfo.theme;
            return getResources().getResourceEntryName(themeResId);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding=ActivityDashboardAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
      //  getSupportActionBar().hide();
        preferenceManager = new PreferenceManager(this);
        Log.d(TAG, "onCreateGetThemeNameBefore: "+getThemeName());



        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        customDialogs = new CustomDialogs(this);
        loadCategories();

        //search edit text change listener
        binding.searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    adapterCategory.getFilter().filter(s);
                }catch(Exception e){
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //handle add category click button , start add category activity
        binding.addCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardAdminActivity.this,CategoryActivity.class));
            }
        });
        //handle logout click
        binding.logoutIVBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();

            }
        });

        //handle pdf floating btn
        binding.addPdfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardAdminActivity.this,PdfAddActivity.class));
            }
        });
    }

    private void loadCategories() {
        categoryArrayList=new ArrayList<>();
        //get Categories from firebase db
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Categories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear array list before storing data into it
                categoryArrayList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    //get data
                    ModelCategory model= ds.getValue(ModelCategory.class);
                    //add to array list
                    categoryArrayList.add(model);
                }
                if(categoryArrayList.size()==0){
                    binding.searchET.setVisibility(View.GONE);
                    binding.emptyDataView.setVisibility(View.VISIBLE);
                }else{
                    binding.searchET.setVisibility(View.VISIBLE);
                    binding.emptyDataView.setVisibility(View.GONE);
                    adapterCategory=new AdapterCategory(DashboardAdminActivity.this,categoryArrayList);
                    binding.categoriesRV.setAdapter(adapterCategory);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showLogoutDialog(){
        customDialogs.customLogoutScreen(this, new CustomDialogs.OnDialogListener( ) {
            @Override
            public void onDialogYes(Dialog dialog) {
                firebaseAuth.signOut();
                checkUser();
            }

            @Override
            public void onDialogNo(Dialog dialog) {

            }
        });

    }

    private void checkUser() {
        //get current user
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        if(firebaseUser==null)
        {
            //user not logged in
            startActivity(new Intent(DashboardAdminActivity.this,SplashActivity.class));
            finish();
        }else{
            //get login details
            String email=firebaseUser.getEmail();
            //set mail in toolbar textview
            binding.subTitleTV.setText(email);
        }
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