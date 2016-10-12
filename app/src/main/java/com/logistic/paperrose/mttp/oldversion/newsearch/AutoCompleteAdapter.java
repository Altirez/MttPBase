package com.logistic.paperrose.mttp.oldversion.newsearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.logistic.paperrose.mttp.oldversion.LoginActivity;
import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;
import com.logistic.paperrose.mttp.oldversion.utils.ConnectionActivity;
import com.logistic.paperrose.mttp.oldversion.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by paperrose on 28.01.2015.
 */
public class AutoCompleteAdapter extends BaseAdapter implements Filterable {

    private static final int MAX_RESULTS = 10;

    public void setActivity(ConnectionActivity activity) {
        this.activity = activity;
    }

    ConnectionActivity activity;
    private final Context mContext;
    private ArrayList<TripleTableField> mResults;

    public AutoCompleteAdapter(Context context) {
        mContext = context;
        mResults = new ArrayList<TripleTableField>();
    }

    @Override
    public int getCount() {
        return mResults.size();
    }

    @Override
    public TripleTableField getItem(int index) {
        return mResults.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.double_autocomplete_item, parent, false);
        }
        TripleTableField book = getItem(position);
        ((TextView) convertView.findViewById(R.id.text1)).setText(book.getText());
        ((TextView) convertView.findViewById(R.id.text2)).setText(ApplicationParameters.getTableFieldNameByKey(book.getName(), ApplicationParameters.tableFields));

        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    List<TripleTableField> books = findBooks(constraint.toString());
                    // Assign the data to the FilterResults
                    filterResults.values = books;
                    filterResults.count = books.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
                if (results != null && results.count > 0) {
                    mResults = (ArrayList<TripleTableField>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }};

        return filter;
    }

    /**
     * Returns a search result for the given book title.
     */
    public String getPref(String key) {
        return mContext.getSharedPreferences("UserPreference", 0).getString(key, "123");
    }

    private ArrayList<TripleTableField> findBooks(final String bookTitle) {
        // GoogleBooksService is a wrapper for the Google Books API
        //GoogleBooksService service = new GoogleBooksService (mContext, MAX_RESULTS);
        final String access_token = getPref("access_token");
        ArrayList<TripleTableField> result = new ArrayList<TripleTableField>();
        final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);

        JSONParser parser = new JSONParser();


        JSONObject obj = parser.getJSONFromUrl("http://"+ApplicationParameters.MAIN_DOMAIN +"/search/autocomplete", new HashMap<String, String>() {{
            put("access_token", access_token);
            put("hs", secure_token);
            put("filter_object", activity.getJSONParameters());
        }}, mContext);
        try {
            JSONArray results = obj.getJSONArray("result");
            for (int i = 0; i < results.length(); i++) {
                JSONObject obj0 = results.getJSONObject(i);
                result.add(new TripleTableField(obj0.getString("field_name"), obj0.getString("field_value")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
