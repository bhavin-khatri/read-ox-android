package com.example.readox.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readox.utils.MyApplication;
import com.example.readox.activities.PdfDetailActivity;
import com.example.readox.databinding.RowPdfUserListBinding;
import com.example.readox.filters.FilterPdfUser;
import com.example.readox.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdfUser extends RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser> implements Filterable {

    private Context context;
    public ArrayList<ModelPdf> pdfArrayList , filterList;
    private FilterPdfUser filter;

    private RowPdfUserListBinding binding;

    private static final String TAG="ADAPTER_PDF_USER_TAG";

    public AdapterPdfUser(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //bind view
        binding=RowPdfUserListBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderPdfUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfUser holder, int position) {
        //get data set data handle click
        //get data
        ModelPdf model=pdfArrayList.get(position);
        String title=model.getTitle();
        String bookId=model.getId();
        String description=model.getDescription();
        String pdfUrl=model.getUrl();
        String categoryId=model.getCategoryId();
        long timestamp=model.getTimestamp();
        //convert to date
        String date= MyApplication.formatTimestamp(timestamp);
        //set data
        holder.titleTV.setText(title);
        holder.descriptionTV.setText(description);
        holder.dateTV.setText(date);

        MyApplication.loadPdfFromUrlSinglePage(
                ""+pdfUrl,
                ""+title,
                holder.pdfView,
                holder.progressbar
        );
        MyApplication.loadCategory(
                ""+categoryId,
                holder.categoryTV
        );
        MyApplication.loadPdfSize(
                ""+pdfUrl,
                ""+title,
                holder.sizeTV
        );

        //handle click show pdf details activity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId",bookId);
                context.startActivity(intent);
            }
        });

        holder.setIsRecyclable(false);

    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter==null){
            filter=new FilterPdfUser(filterList,this);
        }
        return filter;
    }

    class HolderPdfUser extends RecyclerView.ViewHolder{
        TextView titleTV,descriptionTV,categoryTV,sizeTV,dateTV;
        PDFView pdfView;
        ProgressBar progressbar;

        public HolderPdfUser(@NonNull View itemView) {
            super(itemView);
            titleTV=binding.titleTV;
            descriptionTV=binding.descriptionTV;
            categoryTV=binding.categoryTV;
            sizeTV=binding.sizeTV;
            dateTV=binding.dateTV;
            pdfView=binding.pdfView;
            progressbar=binding.progressbar;
        }
    }
}
