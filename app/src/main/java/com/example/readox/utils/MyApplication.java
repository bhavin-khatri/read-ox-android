package com.example.readox.utils;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.example.readox.R;
import com.example.readox.utils.PreferenceManager;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import static com.example.readox.utils.Constants.MAX_BYTES_PDF;

//application class runs before launcher activity
public class MyApplication extends Application {
    private static final String TAG_DOWNLOAD= "DOWNLOAD_TAG"; //download book tag
    private static final String TAG= "MY_APPLICATION_TAG"; //download book tag
    private static PreferenceManager pref;
    @Override
    public void onCreate() {
        super.onCreate();

    }



    //static method to convert timestamp in dd/mm/yyyy format and can use it in anywhere in project
    public static final String formatTimestamp(long timestamp){
        Calendar cal=Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        //format timestamp to dd/mm/yyyy
        String date= DateFormat.format("dd/MM/yyyy",cal).toString();
        return date;
    }

    private int themeId;
    @Override
    public void setTheme(int resid) {
        super.setTheme(resid);
    }

    public int getThemeId() {
        return themeId;
    }

    public static void deleteBook(Context context, String bookId, String bookUrl, String bookTitle) {
        String TAG="DELETE_BOOK_TAG";

        ProgressDialog progressDialog=new ProgressDialog(context, R.style.AppCompatAlertDialogStyle);
        progressDialog.setTitle("Please Wait");
        Log.d(TAG, "deleteBook: Deleting Book...");
        progressDialog.setMessage("Deleting"+bookTitle+"...");
        progressDialog.show();

        //deleting from storage ref
        StorageReference storageReference= FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Deleted from storage");
                        Log.d(TAG, "onSuccess: now deleting info from db");
                        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "onSuccess: Deleted from db too");
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "EBook Deleted Successfully...", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Failed to deleted from db due to"+e.getMessage());
                                        progressDialog.dismiss();
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure:Failed To Delete book from storage due to"+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public static void deleteCategory(Context context,String id,String category,String uid) {
        String TAG="DELETE_CATEGORY_TAG";
        ProgressDialog progressDialog=new ProgressDialog(context,R.style.AppCompatAlertDialogStyle);
        progressDialog.setTitle("Please Wait");
        Log.d(TAG, "deleteBook: Deleting Catgeory...");
        progressDialog.setMessage("Deleting"+category+"...");
        progressDialog.show();

        //deleting from storage ref
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Categories");
        reference.child(id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Deleted from db too");
                        progressDialog.dismiss();
                        Toast.makeText(context, "Category Deleted Successfully...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to deleted from db due to"+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    public static void loadPdfSize(String pdfUrl, String pdfTitle, TextView sizeTV) {
        String TAG="PDF_SIZE_TAG";
        StorageReference ref= FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        //get size in bytes
                        double bytes=storageMetadata.getSizeBytes();
                        Log.d(TAG, "onSuccess: "+pdfTitle+""+bytes);
                        //covert bytes in kb,mb
                        double kb=bytes/1024;
                        double mb=kb/1024;
                        if (mb>=1){
                           sizeTV.setText(String.format("%.2f",mb)+" MB");
                        }
                        else  if (kb>=1){
                            sizeTV.setText(String.format("%.2f",kb)+" KB");
                        }else{
                            sizeTV.setText(String.format("%.2f",bytes)+" bytes");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to get meta data
                        Log.d(TAG, "onFailure: "+e.getMessage());
                    }
                });
    }

    public static void loadPdfFromUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar) {
        String TAG="PDF_LOAD_SINGLE_TAG";
        StorageReference ref= FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: "+pdfTitle+"successfully got the file");
                        //set to pdfView
                        pdfView.fromBytes(bytes)
                                .pages(0) //show only first page
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onError: "+t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onPageError: "+t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "loadComplete: pdf loaded");
                                    }
                                })
                                .load();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "onFailure: Failed getting file from url due to"+e.getMessage());
                    }
                });
    }

    public static void loadCategory(String categoryId,TextView categoryTV) {

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category=""+snapshot.child("category").getValue();
                        categoryTV.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void incrementBookViewCount(String bookId){
        //get book id
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get views count
                        String viewsCount=""+snapshot.child("viewsCount").getValue();
                        //in case views count is null set value 0
                        if(viewsCount.equals("")||viewsCount.equals("null"))
                        {
                            viewsCount="0";
                        }
                        //increment view counts
                        long newViewsCount= Long.parseLong(viewsCount)+1;
                        HashMap<String,Object> hashMap= new HashMap<>();
                        hashMap.put("viewsCount",newViewsCount);
                        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId)
                                .updateChildren(hashMap);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    //for download book !!!

    public static void downloadBook(Context context,String bookId,String bookTitle,String bookUrl){
        Log.d(TAG_DOWNLOAD, "downloadBook: Downloading Book");
        String nameWithExtension= bookTitle +".pdf";
        Log.d(TAG_DOWNLOAD, "downloadBook: Name:"+nameWithExtension);

        //progress dialog
        ProgressDialog progressDialog =new ProgressDialog(context,R.style.AppCompatAlertDialogStyle);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Downloading "+ bookTitle +"...");
        progressDialog.show();

        //download from firebase storage using url

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG_DOWNLOAD, "onSuccess: Book Downloaded");
                        saveDownloadedBook(context,progressDialog,bytes,nameWithExtension,bookId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG_DOWNLOAD, "onFailure: Book Downloading Failed due to"+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, " Book Downloading Failed due to"+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private static void saveDownloadedBook(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtension, String bookId) {
        Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Saving downloaded book");
        try{
            File downloadsFolder =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadsFolder.mkdirs();

            String filePath= downloadsFolder.getPath()+ "/" +nameWithExtension;

            FileOutputStream out=new FileOutputStream(filePath);
            out.write(bytes);
            out.close();

            Toast.makeText(context, "Saved to Download Folder", Toast.LENGTH_SHORT).show();
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Saved to Download Folder");
            progressDialog.dismiss();

            incrementBookDownloadCount(bookId);

        }catch (Exception e){

            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Failed  saving to Download Folder due to "+e.getMessage());
            Toast.makeText(context, "Failed  saving to Download Folder due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();

        }
    }

    private static void incrementBookDownloadCount(String bookId) {
        Log.d(TAG_DOWNLOAD, "incrementBookDownloadCount: Incrementing Book Download Count");
        //get previous book download count
        DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String downloadsCount=""+snapshot.child("downloadsCount").getValue();
                    Log.d(TAG_DOWNLOAD, "onDataChange: Download Counts :"+downloadsCount);
                        if (downloadsCount.equals("")||downloadsCount.equals("null")) {
                            downloadsCount="0";
                        }

                    //convert to long and increment by 1
                        long newDownloadsCount= Long.parseLong(downloadsCount)+1;
                        Log.d(TAG_DOWNLOAD, "onDataChange: New Download count"+newDownloadsCount);

                    //setup data to update
                        HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("downloadsCount",newDownloadsCount);

                    //update new incremented download count to db
                        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId).updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG_DOWNLOAD, "onSuccess: Download Count Updated");

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG_DOWNLOAD, "onFailure: Failed to update Download Count due to "+e.getMessage());


                                    }
                                });



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}
