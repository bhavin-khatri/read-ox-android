package com.example.readox.filters;

import android.widget.Filter;

import com.example.readox.adapters.AdapterPdfUser;
import com.example.readox.models.ModelPdf;

import java.util.ArrayList;

public class FilterPdfUser extends Filter {
    //array list we want to search
    ArrayList<ModelPdf> filterList;
    //adapter in which filter need to be added
    AdapterPdfUser adapterPdfUser;

    //constructor


    public FilterPdfUser(ArrayList<ModelPdf> filterList, AdapterPdfUser adapterPdfUser) {
        this.filterList = filterList;
        this.adapterPdfUser = adapterPdfUser;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results=new FilterResults();
        //value to be searched should not be null/empty
        if(constraint!=null ||constraint.length()>0){
            //change to uppercase to  avoid case sensitivity
            constraint=constraint.toString().toUpperCase();
            ArrayList<ModelPdf> filteredModels=new ArrayList<>();
            for(int i=0;i<filterList.size();i++){
                if(filterList.get(i).getTitle().toUpperCase().contains(constraint)){
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count=filteredModels.size();
            results.values=filteredModels;
        }else{
            results.count=filterList.size();
            results.values=filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //apply filter change
        adapterPdfUser.pdfArrayList=(ArrayList<ModelPdf>)results.values;
        //notify changes
        adapterPdfUser.notifyDataSetChanged();

    }
}
