package com.example.readox.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readox.utils.MyApplication;
import com.example.readox.activities.PdfListAdminActivity;
import com.example.readox.filters.FilterCategory;
import com.example.readox.models.ModelCategory;
import com.example.readox.databinding.RowCategoryBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.HolderCategory> implements Filterable {

    private Context context;
    public ArrayList<ModelCategory> categoryArrayList,filterList;
    private FilterCategory filter;

    //view binding
    private RowCategoryBinding binding;
    public AdapterCategory(Context context, ArrayList<ModelCategory> categoryArrayList) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.filterList = categoryArrayList;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //binding row_category.xml
        binding=RowCategoryBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {
        //get data
        ModelCategory model=categoryArrayList.get(position);

        String id=model.getId();
        String category=model.getCategory();
        String uid=model.getUid();
        long timestamp=model.getTimestamp();
        //set data
        holder.categoryTv.setText(category);
        //handle click delete
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //confirm delete dialog
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this Category?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            //begin delete category
                                Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show();
                                MyApplication.deleteCategory
                                        (context,
                                                id,
                                                category,
                                                uid);
//                                MyApplication.deleteCategory(model,holder);
                                holder.categoryTv.setText(null);
                            }
                        })
                        .setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            }
                        }).show();
            }
        });
        //handle item click , goto pdflistactivity,also pass pdf category and categoryid
     holder.itemView.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             Intent intent=new Intent(context, PdfListAdminActivity.class);
             intent.putExtra("categoryId",id);
             intent.putExtra("categoryTitle",category);
             context.startActivity(intent);
         }
     });
    }

    private void deleteCategory(ModelCategory model, HolderCategory holder) {
        String id=model.getId();
        //get id of category to delete
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(id)
            .removeValue()
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                //category deleted successfully
                    Toast.makeText(context, "Successfully deleted...", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed to delete category
                    Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

    }

    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter==null)
        {
            filter=new FilterCategory(filterList,this);

        }
        return filter;
    }

    //view holder class for holding values for row_category.xml
    class HolderCategory extends RecyclerView.ViewHolder{
        //ui views of row_category.xml
        TextView categoryTv;
        ImageButton deleteBtn;

        public HolderCategory(@NonNull View itemView) {
            super(itemView);
            //init  ui views
            categoryTv=binding.categoryTV;
            deleteBtn=binding.deleteBtn;
        }
    }
}
