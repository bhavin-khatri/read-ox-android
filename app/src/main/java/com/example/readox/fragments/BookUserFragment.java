package com.example.readox.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.readox.adapters.AdapterPdfUser;
import com.example.readox.databinding.FragmentBookUserBinding;
import com.example.readox.models.ModelPdf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
public class BookUserFragment extends Fragment {

    public String categoryId;
    public String category;
    public String uid;
    public ArrayList<ModelPdf> pdfArrayList;
    public AdapterPdfUser adapterPdfUser;

    //view binding
    public FragmentBookUserBinding binding;

    public static final String TAG="BOOKS_USER_TAG";

    public BookUserFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static BookUserFragment newInstance(String categoryId, String category, String uid) {
        BookUserFragment fragment = new BookUserFragment();
        Bundle args = new Bundle();
        args.putString("categoryId",categoryId);
        args.putString("category", category);
        args.putString("uid",uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
            uid = getArguments().getString("uid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentBookUserBinding.inflate(LayoutInflater.from(getContext()),container,false);
        Log.d(TAG, "onCreateView: Category:"+category);
        if (category.equals("All")){
            //load all books
            loadAllBooks();
            
        }
        else if(category.equals("Most Viewed")){
            //load most viewed books
            loadMostViewedDownloadedBooks("viewsCount");

        }
        else if(category.equals("Most Downloaded")){
            //load most downloaded books
            loadMostViewedDownloadedBooks("downloadsCount");
        }
        else {
            //load selected category books
            loadCategorizedBooks();
        }

        //search
        binding.searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    adapterPdfUser.getFilter().filter(s);
                }catch (Exception e){
                    Log.d(TAG, "onTextChanged: "+e.getMessage());
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return binding.getRoot();
    }


    public void loadAllBooks() {
        pdfArrayList =new ArrayList<>();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Books");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();

                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelPdf model=ds.getValue(ModelPdf.class);
                    pdfArrayList.add(model);
                }
                if(pdfArrayList.size()==0){
                    binding.searchET.setVisibility(View.GONE);
                    binding.emptyDataView.setVisibility(View.VISIBLE);
                }else{
                    adapterPdfUser = new AdapterPdfUser(getContext(),pdfArrayList);
                    binding.booksRv.setAdapter(adapterPdfUser);
                    binding.searchET.setVisibility(View.VISIBLE);
                    binding.emptyDataView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void loadMostViewedDownloadedBooks(String orderBy) {
        pdfArrayList =new ArrayList<>();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild(orderBy).limitToLast(10)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();

                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelPdf model=ds.getValue(ModelPdf.class);
                    pdfArrayList.add(model);
                }
                if(pdfArrayList.size()==0){
                    binding.searchET.setVisibility(View.GONE);
                    binding.emptyDataView.setVisibility(View.VISIBLE);
                }else{
                    adapterPdfUser = new AdapterPdfUser(getContext(),pdfArrayList);
                    binding.booksRv.setAdapter(adapterPdfUser);
                    binding.searchET.setVisibility(View.VISIBLE);
                    binding.emptyDataView.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void loadCategorizedBooks() {
        pdfArrayList =new ArrayList<>();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();

                        for(DataSnapshot ds:snapshot.getChildren()){
                            ModelPdf model=ds.getValue(ModelPdf.class);
                            pdfArrayList.add(model);
                        }
                        if(pdfArrayList.size()==0){
                            binding.searchET.setVisibility(View.GONE);
                            binding.emptyDataView.setVisibility(View.VISIBLE);
                        }else{
                            adapterPdfUser = new AdapterPdfUser(getContext(),pdfArrayList);
                            binding.booksRv.setAdapter(adapterPdfUser);
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