package com.logistic.paperrose.mttp.oldversion.newsearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.TextView;

import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;
import com.logistic.paperrose.mttp.oldversion.utils.ColoredArrayAdapter;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by paperrose on 29.01.2015.
 */
public class SearchPageAdapter extends ColoredArrayAdapter<TripleTableField> {
    public ArrayList<TripleTableField> objects = new ArrayList<TripleTableField>();
    LayoutInflater lInflater;
    public Integer pageID = -1;
    public Integer statusID = -1;

    private boolean is_changed = false;

    public void isChanged(boolean value) {
        is_changed = value;
    }

    public boolean isChanged() {
        return is_changed;
    }

    public SearchPageAdapter(Context context, int textViewResourceId, ArrayList<TripleTableField> objects) {

        super(context, textViewResourceId, objects);
        this.objects = objects;
        filteredItems = new ArrayList<TripleTableField>(objects);
        lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
    @Override
    public int getCount() {
        return filteredItems.size();
        //return objects.size();
        //      return objects.size() - ApplicationParameters.chosenTableFields.size();
    }

    // элемент по позиции
    @Override
    public TripleTableField getItem(int position) {
        //return objects.get(position);
        return filteredItems.get(position);
    }

    public String getPageBody() {
        String res = "";
        for (int i = 0; i < objects.size(); i++) {
            TripleTableField p = objects.get(i);
            res += p.getName() + " : ";
            if (p.getType().equals("date")) {
                res += getDateFromTimestamp(p.getText());
            } else {
                if (ApplicationParameters.getTableFieldKeyByName(p.getName(), ApplicationParameters.tableFields).equals("ID_STATUS_EX2")) {
                    try {
                        res += (new String(p.getText().getBytes("cp1251")));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    res += (p.getText());
                }
            }
            res += "\n";
        }
        return res;
    }
    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {// objects.size() - ApplicationParameters.chosenTableFields.size()) {
            view = lInflater.inflate(R.layout.search_field_row, parent, false);

        }
        TripleTableField p = getItem(position);
        ((TextView)view.findViewById(R.id.name)).setText(p.getName());
        EditText tv = (EditText) view.findViewById(R.id.val);

        tv.setLongClickable(true);
        tv.setOnCreateContextMenuListener(null);
        if (p.getType().equals("date")) {
            tv.setText(getDateFromTimestamp(p.getText()));
        } else {
            if (ApplicationParameters.getTableFieldKeyByName(p.getName(), ApplicationParameters.tableFields).equals("ID_STATUS_EX2")) {
                try {
                    tv.setText(new String(p.getText().getBytes("cp1251")));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                tv.setText(p.getText());
            }
        }
        int colorPos = position % ApplicationParameters.colors.length;
        view.setBackgroundColor(ApplicationParameters.colors[colorPos]);
        return view;
    }

    public String getDateFromTimestamp(String stamp) {
        if (stamp.isEmpty() || stamp == null) return "";
        long unixSeconds = new Long(stamp);
        Date date = new Date(unixSeconds*1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy"); // the format of your date
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+4")); // give a timezone reference for formating (see comment at the bottom
        String formattedDate = sdf.format(date);
        return formattedDate;
    }
    private ItemFilter mFilter = new ItemFilter();
    public Filter getFilter() {
        return mFilter;
    }

    private ArrayList<TripleTableField> filteredItems = null;
    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<TripleTableField> list = objects;

            int count = list.size();
            final ArrayList<TripleTableField> nlist = new ArrayList<TripleTableField>(count);

            TripleTableField filterableString ;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i);
                if (filterableString.getName().toLowerCase().contains(filterString) || filterableString.getText().toLowerCase().contains(filterString)) {
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
