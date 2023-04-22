package com.example.readox.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readox.utils.MyApplication;
import com.example.readox.R;
import com.example.readox.activities.PdfDetailActivity;
import com.example.readox.activities.EditPdfActivity;
import com.example.readox.databinding.RowPdfAdminListBinding;
import com.example.readox.filters.FilterPdfAdmin;
import com.example.readox.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class  AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {

    //context
    private Context context;
    //array to hold list of data type model pdf
    public ArrayList<ModelPdf> pdfArrayList,filterList;
    //view binding row_pdf_admin
    private RowPdfAdminListBinding  binding;
    //progress dialog
    private ProgressDialog progressDialog;
    //tag
    private static final String TAG="PDF_ADAPTER_TAG";
    //constructor
    private FilterPdfAdmin filter;
    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList=pdfArrayList;
        //init progress dialog
        progressDialog=new ProgressDialog(context, R.style.AppCompatAlertDialogStyle);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        //bind layout using view binding
        binding=RowPdfAdminListBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {

        //get data
        ModelPdf model=pdfArrayList.get(position);
        String pdfId=model.getId();
        String categoryId=model.getCategoryId();
        String title=model.getTitle();
        String description=model.getDescription();
        long timestamp=model.getTimestamp();
        String pdfUrl=model.getUrl();

        //convert timestamp in dd/mm/yyyy format
        String formattedDate= MyApplication.formatTimestamp(timestamp);
        holder.titleTV.setText(title);
        holder.descriptionTV.setText(description);
        holder.dateTV.setText(formattedDate);
        //load other details in other function
        MyApplication.loadCategory(
                ""+categoryId,
                holder.categoryTV);
        MyApplication.loadPdfFromUrlSinglePage(
                ""+pdfUrl,
                ""+title,
                holder.pdfView,
                holder.progressBar
        );
        MyApplication.loadPdfSize(
                ""+pdfUrl,
                ""+title,
                 holder.sizeTV
        );

        //handle more button
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionsDialog(model,holder);
            }
        });

        //handle pdf click , open book details
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId",pdfId);
                context.startActivity(intent);
            }
        });
        holder.setIsRecyclable(false);
    }


    private void moreOptionsDialog(ModelPdf model, HolderPdfAdmin holder) {
        String bookId=model.getId();
        String bookUrl=model.getUrl();
        String bookTitle=model.getTitle();
        //options to show in dialog
        String[] options ={"Edit","Delete"};
        //alert dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    if(which==0)
                    {
                        //edit clicked
                        Intent intent=new Intent(context, EditPdfActivity.class);
                        intent.putExtra("bookId",bookId);
                        context.startActivity(intent);
                    }else if(which==1){
                        //delete clicked
                        MyApplication.deleteBook
                                (context,
                                        ""+bookId,
                                        ""+bookUrl,
                                        ""+bookTitle);
                        //deleteBook(model,holder);
                        }
                    }
                })
                .show();
    }









    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter==null)
        {
            filter=new FilterPdfAdmin(filterList,this);
        }
        return filter;
    }


    //view holder for row_pdf_list_admin.xml

    class HolderPdfAdmin extends RecyclerView.ViewHolder{
        //ui views of row_pdf_list_admin.xml
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTV;
        TextView descriptionTV;
        TextView categoryTV;
        TextView sizeTV;
        TextView dateTV;
        ImageButton moreBtn;

        public HolderPdfAdmin(@NonNull View itemView) {

            super(itemView);
            //init ui views
            pdfView=binding.pdfView;
            progressBar=binding.progressbar;
            titleTV=binding.titleTV;
            descriptionTV=binding.descriptionTV;
            categoryTV=binding.categoryTV;
            sizeTV=binding.sizeTV;
            dateTV=binding.dateTV;
            moreBtn=binding.moreBtn;

        }
    }
}
