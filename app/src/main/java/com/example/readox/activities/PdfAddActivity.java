package com.example.readox.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.readox.R;
import com.example.readox.databinding.ActivityPdfAddBinding;
import com.example.readox.utils.UtilsMethod;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {

    //setup view binding
    private ActivityPdfAddBinding binding;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //pdf pick code
    private static final int PDF_PICK_CODE=10;
    //tag for debugging
    private static final String TAG="ADD_PDF_TAG";

    //uri of pdf picked
    private Uri pdfUri=null;
    //array list to hold pdf categories
    private ArrayList<String> categoryTitleArrayList,categoryIdArrayList;
    //progress dialog
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
       // getSupportActionBar().hide();

        //init firebase auth
        firebaseAuth=FirebaseAuth.getInstance();
        loadPdfCategories();

        //setup progress dialog
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
        //handle attach pdf button
        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfPickIntent();
            }
        });
        //handle pick categories text view
        binding.categoryTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryPickDialog();
            }
        });
        //handle submit button
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    String title="",description="";
    private void validateData() {

        Log.d(TAG, "validateData: Validating data...");
        //get data
        title=binding.bookET.getText().toString().trim();
        description=binding.descriptionET.getText().toString().trim();
        //validate data
        if(TextUtils.isEmpty(title)){
            Toast.makeText(this, R.string.errEnterBookTitle, Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(description))
        {
            Toast.makeText(this, R.string.errEnterBookDesc, Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(selectedCategoryTitle))
        {
            Toast.makeText(this, R.string.errSelectBookCategory, Toast.LENGTH_SHORT).show();
        }else if(pdfUri==null)
        {
            Toast.makeText(this, R.string.errAttachBook, Toast.LENGTH_SHORT).show();
        }else{
            //all data is valid, upload now
            uploadPdfToStorage();

        }

    }

    private void uploadPdfToStorage() {
        //method to upload pdf in fire base storage
        Log.d(TAG, "uploadPdfToStorage: uploading to storage...");
        //show progress dialog
        progressDialog.setMessage("Uploading EBook...");
        progressDialog.show();
        //time stamp
        long timestamp=System.currentTimeMillis();
        //path of pdf in firebase storage
        String filePathAndName="Books/"+timestamp;
        //storage reference
        StorageReference storageReference= FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Log.d(TAG, "onSuccess: EBook Uploaded Successfully in Storage...");
                        Log.d(TAG, "onSuccess: getting EBook Url");
                        //get pdf url
                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedPdfUrl=""+uriTask.getResult();
                        //upload to firebase db
                        uploadPdfInfoToDb(uploadedPdfUrl,timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        //failed to store pdf  in storage
                        Log.d(TAG, "onFailure: EBook upload failed due to"+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, R.string.msgBookUploadFailed+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });



    }

    private void uploadPdfInfoToDb(String uploadedPdfUrl, long timestamp) {
        //upload pdf info to firebase db
        Log.d(TAG, "uploadPdfToStorage: uploading EBook info to firebase db...");
        progressDialog.setMessage("Uploading EBook info...");
        String uid=firebaseAuth.getUid();
        //setup data to  upload
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id",""+timestamp);
        hashMap.put("title",""+title);
        hashMap.put("description",""+description);
        hashMap.put("categoryId",""+selectedCategoryId);
        hashMap.put("url",""+uploadedPdfUrl);
        hashMap.put("timestamp",timestamp);
        hashMap.put("viewsCount",0);
        hashMap.put("downloadsCount",0);

        //database reference db>Books
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Books");
        ref.child(""+timestamp)
            .setValue(hashMap)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progressDialog.dismiss();
                    Log.d(TAG, "onSuccess: Successfully Uploaded EBook File...");
                    Toast.makeText(PdfAddActivity.this, R.string.msgBookUploadSuccess, Toast.LENGTH_SHORT).show();
                    UtilsMethod.sendNotificationNewBook(PdfAddActivity.this,title);
                    binding.bookET.setText("");
                    binding.descriptionET.setText("");
                    binding.categoryTV.setText("");
                    binding.txtUploadedBook.setVisibility(View.GONE);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Log.d(TAG, "onFailure: Failed to upload EBook File due to"+e.getMessage());
                    Toast.makeText(PdfAddActivity.this, R.string.msgBookUploadFailed+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void loadPdfCategories() {
        //method to load pdf categories from firebase db
        Log.d(TAG, "loadPdfCategories: Loading EBook Categories...");
        categoryTitleArrayList =new ArrayList<>();
        categoryIdArrayList=new ArrayList<>();
        //db reference to load categories
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();//clear before adding
                categoryIdArrayList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    //get id and title of category
                  String categoryId=""+ds.child("id").getValue();
                  String categoryTitle=""+ds.child("category").getValue();
                  //add to respective arraylist
                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    //selected id and category from arraylist
    private String selectedCategoryId,selectedCategoryTitle;
    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: showing category pick dialog");
        //get string array of categories from array list
        String[] categoriesArray=new String[categoryTitleArrayList.size()];
        for(int i = 0; i< categoryTitleArrayList.size(); i++){
            categoriesArray[i]= categoryTitleArrayList.get(i);
        }
        //alert dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(R.string.labelSelectBookCategory)
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCategoryTitle=categoryTitleArrayList.get(which);
                        selectedCategoryId=categoryIdArrayList.get(which);
                        binding.categoryTV.setText(selectedCategoryTitle);
                        Log.d(TAG, "onClick: Selected Category:"+selectedCategoryId+""+selectedCategoryTitle);
                    }
                }).show();


    }

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting EBook pick intent");
        Intent intent=new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select EBook"),PDF_PICK_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RESULT_OK){
            if(requestCode!=PDF_PICK_CODE)
            {
                Log.d(TAG, "onActivityResult: Canceled Picking EBook");
                Toast.makeText(this, R.string.errCancelPickingBook, Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Log.d(TAG, "onActivityResult: EBook Picked");
            pdfUri=data.getData();
            String uriString = pdfUri.toString();
            File myFile = new File(uriString);
            String path = myFile.getAbsolutePath();
            String displayName = null;
            if (uriString.startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = getApplicationContext().getContentResolver().query(pdfUri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor.close();
                }
            } else if (uriString.startsWith("file://")) {
                displayName = myFile.getName();
            }
            binding.txtUploadedBook.setText(displayName.toString());
            binding.txtUploadedBook.setVisibility(View.VISIBLE);
            Log.d(TAG, "onActivityResult: displayName:"+displayName);
        }

            Log.d(TAG, "onActivityResult: URI:"+pdfUri);
        }
    }