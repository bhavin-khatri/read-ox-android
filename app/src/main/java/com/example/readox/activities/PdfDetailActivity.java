package com.example.readox.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.readox.utils.MyApplication;
import com.example.readox.databinding.ActivityPdfDetailBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PdfDetailActivity extends AppCompatActivity {
    //view binding
    private ActivityPdfDetailBinding binding;
    //get book id from intent
    String bookId , bookTitle,bookUrl;
    private static final String TAG_DOWNLOAD= "DOWNLOAD_TAG"; //download book tag


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
      //  getSupportActionBar().hide();
        //get data from intent
        Intent intent=getIntent();
        bookId= intent.getStringExtra("bookId"); //getiing book id from adapterpdfamin , item click event
        //hide download button until book details load from url from   loadBookDetails();
        binding.downloadBookBtn.setVisibility(View.GONE);

        loadBookDetails();
        //increment views count
        MyApplication.incrementBookViewCount(""+bookId);
        //back btn click handle
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // handle read btn click
        binding.readBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent1=new Intent(PdfDetailActivity.this,PdfViewActivity.class);
            intent1.putExtra("bookId",bookId);
            startActivity(intent1);
            }
        });
        //handle button download click
        binding.downloadBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG_DOWNLOAD, "onClick: Checking permission");
                if(ContextCompat.checkSelfPermission(PdfDetailActivity.this,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG_DOWNLOAD, "onClick: Permission already granted can download book");
                    MyApplication.downloadBook(PdfDetailActivity.this,""+bookId,""+bookTitle,""+bookUrl);
                }else{
                    Log.d(TAG_DOWNLOAD, "onClick: Ask for Permission");
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                }

            }
        });
    }

    //request storage permission for download book
    private ActivityResultLauncher<String> requestPermissionLauncher=
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),isGranted->{
               if(isGranted)
               {
                   Log.d(TAG_DOWNLOAD, ": Permission Granted");
                   MyApplication.downloadBook(this,""+bookId,""+bookTitle,""+bookUrl);
               }else {
                   Log.d(TAG_DOWNLOAD, "Permission was denied: ");
                   Toast.makeText(this, "Permission was Denied", Toast.LENGTH_SHORT).show();
               }
            });

    private void loadBookDetails() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        bookTitle=""+snapshot.child("title").getValue();
                        String description=""+snapshot.child("description").getValue();
                        String categoryId=""+snapshot.child("categoryId").getValue();
                        String viewsCount=""+snapshot.child("viewsCount").getValue();
                        String downloadsCount=""+snapshot.child("downloadsCount").getValue();
                        bookUrl=""+snapshot.child("url").getValue();
                        String timestamp=""+snapshot.child("timestamp").getValue();

                        //now show the download button as book details loaded from url from   loadBookDetails();
                        binding.downloadBookBtn.setVisibility(View.VISIBLE);

                        //format date
                        String date=MyApplication.formatTimestamp(Long.parseLong(timestamp));

                       MyApplication.loadCategory(
                               ""+categoryId,
                               binding.categoryTV
                       );
                       MyApplication.loadPdfFromUrlSinglePage(
                               ""+bookUrl,
                               ""+bookTitle,
                               binding.pdfView,
                               binding.progressbar
                       );
                       MyApplication.loadPdfSize(
                               ""+bookUrl,
                               ""+bookTitle,
                               binding.sizeTV);
                        //set data
                        binding.titleTV.setText(bookTitle);
                        binding.descriptionTV.setText(description);
                        binding.dateTV.setText(date);
                        binding.viewsTV.setText(viewsCount.replace("null","N/A"));
                        binding.downloadsTV.setText(downloadsCount.replace("null","N/A"));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}