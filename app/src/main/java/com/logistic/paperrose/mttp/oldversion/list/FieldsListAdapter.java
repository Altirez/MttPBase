package com.logistic.paperrose.mttp.oldversion.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;

import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;
import com.logistic.paperrose.mttp.oldversion.utils.ColoredArrayAdapter;

import java.util.ArrayList;

/**
 * Created by paperrose on 23.04.2015.
 */
public class FieldsListAdapter extends ColoredArrayAdapter<TripleTableField> implements Filterable {

    public ArrayList<TripleTableField> filteredItems;
    public ArrayList<TripleTableField> items;
    public CharSequence filterS = "";
    private ItemFilter mFilter = new ItemFilter();
    BaseChosenListAdapter chosenAdapter;
    Context context;
    public FieldsListAdapter(Context context, int textViewResourceId, ArrayList<TripleTableField> objects) {
        super(context, textViewResourceId, objects);
        items = objects;
        this.context = context;
        filteredItems = new ArrayList<TripleTableField>(items);
        lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public Filter getFilter() {
        return mFilter;
    }

    public void setChosenAdapter(BaseChosenListAdapter chosenAdapter) {
        this.chosenAdapter = chosenAdapter;
        filteredItems = refreshItems();
        notifyDataSetChanged();
        this.chosenAdapter.notifyDataSetChanged();
    }

    public void selectAll() {
        chosenAdapter.items.addAll(items);
        refresh();
    }

    public void refresh() {
        filteredItems = refreshItems();
        notifyDataSetChanged();
        chosenAdapter.notifyDataSetChanged();
    }

    public ArrayList<TripleTableField> refreshItems() {
        ArrayList<TripleTableField> temp = new ArrayList<TripleTableField>();
        for (int i = 0; i < items.size(); i++) {
            try {
                boolean b = false;
                for (int j = 0; j < chosenAdapter.items.size(); j++) {
                    if (chosenAdapter.items.get(j).getName().equals(items.get(i).getName())) {
                        b = true;
                    }
                }
                if (b) continue;
                temp.add(items.get(i));
            } catch (NullPointerException e) {
              //  e.printStackTrace();
            }

        }
        return temp;
    }

    @Override
    public int getCount() {
        return filteredItems.size();
    }

    // элемент по позиции
    @Override
    public TripleTableField getItem(int position) {
        return filteredItems.get(position);
    }

    class ViewHolder {
        CheckBox cb;
    }
    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    public LayoutInflater lInflater;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = lInflater.inflate(R.layout.text_edit_list_layout3, null);
            holder = new ViewHolder();
            holder.cb = (CheckBox) convertView.findViewById(R.id.field);
            holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    int getPosition = (Integer) compoundButton.getTag();
                    if (b) {
                        TripleTableField tf = getItem(getPosition);
                        chosenAdapter.items.add(tf);
                        refresh();

                        getFilter().filter(filterS.toString());
                    }
                }
            });
            convertView.setTag(holder);
            convertView.setTag(R.id.field, holder.cb);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.cb.setTag(position);
        holder.cb.setText(getItem(position).getText());
        holder.cb.setChecked(false);
        int colorPos = position % ApplicationParameters.colors.length;
        convertView.setBackgroundColor(ApplicationParameters.colors[colorPos]);
      //  convertView = super.getView(position, convertView, parent);
        return convertView;
    }


    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            final ArrayList<TripleTableField> list = refreshItems();
            int count = list.size();
            final ArrayList<TripleTableField> nlist = new ArrayList<TripleTableField>(count);
            TripleTableField filterableString ;
            for (int i = 0; i < count; i++) {
                filterableString = list.get(i);
                if (filterableString.getText().toLowerCase().contains(filterString)) {
                    nlist.add(filterableString);
                }
            }
            results.values = nlist;
            results.count = nlist.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
            filteredItems = (ArrayList<TripleTableField>) results.values;
            notifyDataSetChanged();
        }

    }
}
