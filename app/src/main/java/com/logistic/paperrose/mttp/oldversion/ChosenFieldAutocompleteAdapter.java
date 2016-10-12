package com.logistic.paperrose.mttp.oldversion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.settings.TableField;

import java.util.ArrayList;

/**
 * Created by paperrose on 16.01.2015.
 */
public class ChosenFieldAutocompleteAdapter extends ArrayAdapter<TableField> {
   /// private final String MY_DEBUG_TAG = "CustomerAdapter";
    private ArrayList<TableField> items;
    private ArrayList<TableField> itemsAll;
    private ArrayList<TableField> suggestions;
    private int viewResourceId;

    public ChosenFieldAutocompleteAdapter(Context context, int viewResourceId, ArrayList<TableField> items) {
        super(context, viewResourceId, items);
        this.items = items;
        this.itemsAll = (ArrayList<TableField>) items.clone();
        this.suggestions = new ArrayList<TableField>();
        this.viewResourceId = viewResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(viewResourceId, null);
        }
        TableField customer = items.get(position);
        if (customer != null) {
            TextView customerNameLabel = (TextView) v.findViewById(R.id.autoTextLabel);
            if (customerNameLabel != null) {
//              Log.i(MY_DEBUG_TAG, "getView Customer Name:"+customer.getName());
                customerNameLabel.setText(customer.getText());
            }
        }
        return v;
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    Filter nameFilter = new Filter() {
        @Override
        public String convertResultToString(Object resultValue) {
            String str = ((TableField)(resultValue)).getText();
            return str;
        }
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if(constraint != null) {
                suggestions.clear();
                for (TableField customer : itemsAll) {
                    if(customer.getText().toLowerCase().contains(constraint.toString().toLowerCase())){
                        suggestions.add(customer);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<TableField> filteredList = (ArrayList<TableField>) results.values;
            if(results != null && results.count > 0) {
                clear();
                for (TableField c : filteredList) {
                    add(c);
                }
                notifyDataSetChanged();
            }
        }
    };

}
