package com.logistic.paperrose.mttp.oldversion.newsearch;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.logistic.paperrose.mttp.oldversion.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by paperrose on 02.03.2015.
 */
public class DictionaryAutoCompleteAdapter  extends BaseAdapter implements Filterable {

    private static final int MAX_RESULTS = 10;

    //public void setActivity(ConnectionActivity activity) {
   //     this.activity = activity;
   // }

   // ConnectionActivity activity;
    private final Context mContext;
    private ArrayList<TripleTableField> mResults;
    TripleTableField dictOptions;
    private boolean isStatus = false;

    public DictionaryAutoCompleteAdapter(Context context, TripleTableField dictOptions) {
        mContext = context;
        mResults = new ArrayList<TripleTableField>();
        if (dictOptions.getName().equals("ID_STATUS_EX2")) {
            isStatus = true;
            mResults = new ArrayList<TripleTableField>(ApplicationParameters.statuses);
        }
        this.dictOptions = dictOptions;
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

        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    List<TripleTableField> books;
                    if (isStatus) {
                        books = findStatuses(constraint.toString());
                    } else {
                        books = findBooks(constraint.toString());
                    }
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

    public void saveCredentials(String hash, String key) {
        SharedPreferences mPrefs = mContext.getSharedPreferences("UserPreference", 0);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(key, hash);
        editor.commit();
    }

    private ArrayList<TripleTableField> findStatuses(final String bookTitle) {
        // GoogleBooksService is a wrapper for the Google Books API
        //GoogleBooksService service = new GoogleBooksService (mContext, MAX_RESULTS);
        final String access_token = getPref("access_token");
        ArrayList<TripleTableField> result = new ArrayList<TripleTableField>();
        final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
        ArrayList<TripleTableField> results = new ArrayList<TripleTableField>(ApplicationParameters.statuses);
        for (int i = 0; i < results.size(); i++) {
            TripleTableField tt = results.get(i);
            if (tt.getText().toUpperCase().contains(bookTitle.toUpperCase()))
                result.add(new TripleTableField(tt.getName(), tt.getText()));
        }
        return result;
    }


    private ArrayList<TripleTableField> findBooks(final String bookTitle) {
        // GoogleBooksService is a wrapper for the Google Books API
        //GoogleBooksService service = new GoogleBooksService (mContext, MAX_RESULTS);
        final String access_token = getPref("access_token");
        ArrayList<TripleTableField> result = new ArrayList<TripleTableField>();
        final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);

        JSONParser parser = new JSONParser();


        JSONObject obj = parser.getJSONFromUrl("http://"+ApplicationParameters.MAIN_DOMAIN +"/search/dictionary_search", new HashMap<String, String>() {{
            put("access_token", access_token);
            put("hs", secure_token);
            put("table_name", dictOptions.getType());
            put("field_name", dictOptions.getText());
            put("filter_text", bookTitle);
        }}, mContext);
        try {
            saveCredentials(obj.getString("access_token"), "access_token");
            JSONArray results = obj.getJSONArray("result");
            for (int i = 0; i < results.length(); i++) {
                JSONObject obj0 = results.getJSONObject(i);
                result.add(new TripleTableField(obj0.getString("ID"), obj0.getString(dictOptions.getText())));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
