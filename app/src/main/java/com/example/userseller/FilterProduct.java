package com.example.userseller;

import android.widget.Filter;

import com.example.userseller.adapters.AdapterProductSeller;
import com.example.userseller.models.ModelProduct;

import java.util.ArrayList;

public class FilterProduct extends Filter {

    private AdapterProductSeller adapter;
    private ArrayList<ModelProduct> filterLists;

    public FilterProduct(AdapterProductSeller adapter,ArrayList<ModelProduct>filterLists){
        this.adapter=adapter;
        this.filterLists=filterLists;
    }


    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results=new FilterResults();
        //validate data for search query
        if (constraint!=null && constraint.length()>0){
            //search filed not empty,searching something, perform search

            //chage to uppercase to make case insensitive
            constraint=constraint.toString().toUpperCase();
            //store our filtered list
            ArrayList<ModelProduct> filteredModels=new ArrayList<>();
            for (int i=0;i<filterLists.size();i++){
                //check search by title and category
                if (filterLists.get(i).getProductTitle().toUpperCase().contains(constraint)||
                filterLists.get(i).getProductCategory().toUpperCase().contains(constraint)){
                    //add filtered data to list
                    filteredModels.add(filterLists.get(i));
                }
            }
            results.count=filteredModels.size();
            results.values=filteredModels;
        }
        else {
            //search filed not empty,not searching,return original/all/complete list

            results.count=filterLists.size();
            results.values=filterLists;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.productList=(ArrayList<ModelProduct>)results.values;
        //refresh adapter
        adapter.notifyDataSetChanged();
    }
}
