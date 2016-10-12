package com.logistic.paperrose.mttp.oldversion.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.settings.SingleField;
import com.logistic.paperrose.mttp.oldversion.utils.ColoredArrayAdapter;

import java.util.ArrayList;

/**
 * Created by paperrose on 27.01.2015.
 * адаптер для autocomplete поля
 */
public class FilteringFieldAdapter extends ColoredArrayAdapter<SingleField> {
    private ArrayList<SingleField> items;
    private ArrayList<SingleField> itemsAll;
    private ArrayList<SingleField> suggestions;
    private int viewResourceId;

    public FilteringFieldAdapter(Context context, int textViewResourceId, ArrayList<SingleField> objects) {
        super(context, textViewResourceId, objects);
        this.items = objects;
        this.itemsAll = new ArrayList<SingleField>(objects);
        this.suggestions = new ArrayList<SingleField>();
        this.viewResourceId = textViewResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(viewResourceId, null);
        }
        SingleField customer = items.get(position);
        if (customer != null) {
            TextView customerNameLabel = (TextView) v.findViewById(R.id.autoTextLabel);
            if (customerNameLabel != null) {
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
            String str = ((SingleField)(resultValue)).getText();
            return str;
        }
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if(constraint != null) {
                suggestions.clear();
                for (SingleField customer : itemsAll) {
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
            ArrayList<SingleField> filteredList = (ArrayList<SingleField>) results.values;
            if(results != null && results.count > 0) {
                clear();
                for (SingleField c : filteredList) {
                    add(c);
                }
                notifyDataSetChanged();
            }
        }
    };
}
