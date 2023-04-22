package com.example.readox.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;

import com.example.readox.R;
import com.example.readox.adapters.AdapterPdfAdmin;
import com.example.readox.databinding.ActivityPdfListAdminBinding;
import com.example.readox.models.ModelPdf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PdfListAdminActivity extends AppCompatActivity {

    //view binding
    private ActivityPdfListAdminBinding binding;
    //array list to hold data
    private ArrayList<ModelPdf> pdfArrayList;
    //adapter
    private AdapterPdfAdmin adapterPdfAdmin;
    private String categoryId,categoryTitle;
    private static final String TAG="PDF_LIST_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityPdfListAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
       // getSupportActionBar().hide();
        //get data from intent
        Intent intent=getIntent();
        categoryId=intent.getStringExtra("categoryId");
        categoryTitle=intent.getStringExtra("categoryTitle");
        binding.subtitleTV.setText(categoryTitle);
        loadPdfList();

        //search button click
        //search edit text change listener
        binding.searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    adapterPdfAdmin.getFilter().filter(s);
                }catch(Exception e){
                    Log.d(TAG, "onTextChanged: "+e.getMessage());
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //back btn click handle
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadPdfList() {
        //init list
        pdfArrayList=new ArrayList<>();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                    pdfArrayList.clear();
                    for(DataSnapshot ds:snapshot.getChildren())
                    {
                        ModelPdf model=ds.getValue(ModelPdf.class);
                        pdfArrayList.add(model);
                        //adapter
                        Log.d(TAG, "onDataChange: "+model.getId()+""+model.getTitle());
                    }
                        Log.d(TAG, "onDataChangePdfArrayListSize: "+pdfArrayList.size());
                        if(pdfArrayList.size()==0){
                            binding.searchET.setVisibility(View.GONE);
                            binding.emptyDataView.setVisibility(View.VISIBLE);
                        }else{
                            adapterPdfAdmin=new AdapterPdfAdmin(PdfListAdminActivity.this,pdfArrayList);
                            binding.bookRv.setAdapter(adapterPdfAdmin);
                            binding.searchET.setVisibility(View.VISIBLE);
                            binding.emptyDataView.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}