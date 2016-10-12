package com.logistic.paperrose.mttp.oldversion.results;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.logistic.paperrose.mttp.oldversion.BaseLogisticActivity;
import com.logistic.paperrose.mttp.oldversion.LoginActivity;
import com.logistic.paperrose.mttp.oldversion.MainActivity;
import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;
import com.logistic.paperrose.mttp.oldversion.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;

public class SearchResults extends BaseLogisticActivity {
    TableLayout results;
    public JSONArray jsonResults;
    public int bookmarkType;
    String jsonString;
    String lastSort = "";
    int lastSortOrder = -1;
    String tableOrderStr = "";
    String singleOrderStr = "";
    JSONArray tempJsonResults = new JSONArray();
    int actType = 0;
    LinearLayout ll;
    LinearLayout lf;
    String sortedBy = "";
    ArrayList<TripleTableField> tableOrder = new ArrayList<TripleTableField>();
    ArrayList<TripleTableField> singleOrder = new ArrayList<TripleTableField>();

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ApplicationParameters.activity = this;
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                return null;
            }

            @Override
            protected void onPostExecute(Void msg) {
                formTable(tempJsonResults);
                View tv = findViewById(R.id.emptyRecords);
                if (tempJsonResults.length() == 0) {

                    tv.setVisibility(View.VISIBLE);
                } else {
                    tv.setVisibility(View.GONE);
                }
                try {
                    findViewById(R.id.fixedHeaders).setVisibility(View.GONE);
                } catch (NullPointerException e) {

                }
                lf.addView(row);
                ll.addView(results, 0);
                setPrevNext();
                showProgress(false);
            }
        }.execute(null, null, null);
    }
    public int offset = 0;
    int maxRow = 25;
    private int getMinNum() {
        return (maxRow*(offset+1) > ApplicationParameters.currentLength) ? ApplicationParameters.currentLength : maxRow*(offset+1);
    }

    private void setTempJsonResults() throws JSONException {
        tempJsonResults = new JSONArray();
        for (int i = maxRow*offset; i < getMinNum(); i++) {
            tempJsonResults.put(jsonResults.getJSONObject(i - (offset/4)*100));
        }
        //if (lastSort != "") {
        //    sortBy(lastSort, lastSortOrder);
        //}
    }
    public int bookmarkID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLayoutId(R.layout.activity_search_results);
        jsonResults = ApplicationParameters.lastResults;
        String order = getIntent().getStringExtra("order");
        bookmarkType = getIntent().getIntExtra("bookmarkType", 0);
        actType = getIntent().getIntExtra("act_type", 0);
        ApplicationParameters.chosenDeclarationType = "all";
        bookmarkID = getIntent().getIntExtra("bookmark_id", -1);
        if (order != null && !(order.isEmpty())) {
            String[] orders = order.split("<<<");
            tableOrderStr = orders[0];
            singleOrderStr = orders[1];
            String[] tOrders = tableOrderStr.split(">");
            for (int i = 0; i < tOrders.length; i++) {
                tableOrder.add(ApplicationParameters.getTableFieldById(Integer.parseInt(tOrders[i]), ApplicationParameters.tableFields));
            }
        } else {
            tableOrder = ApplicationParameters.chosenSearchResultsTableFields;

        }
        if (tableOrder.size() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Предупреждение");
            builder.setMessage("В свойствах вида таблицы не выбрано ни одного поля");
            builder.setCancelable(true);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() { // Кнопка ОК
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss(); // Отпускает диалоговое окно
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            for (int i = 0; i < tableOrder.size(); i++) {
                headers.put(i, 0);
            }
        }
        try {

            setTempJsonResults();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);

        ll = (LinearLayout)findViewById(R.id.table_layout);
        lf = (LinearLayout)findViewById(R.id.fixedHeadersLayout);
        mProgressView = (RelativeLayout)findViewById(R.id.disableLayout);
        results =  (TableLayout)findViewById(R.id.search_results);
        row = (TableRow)findViewById(R.id.fixedHeaders);



      //  row = (TableRow)findViewById(R.id.fixedHeaders);
        registerInBackground();
        if (jsonResults != null && jsonResults.length() == 1) {
            Intent intent = new Intent(SearchResults.this, ViewPagerSearchActivity.class);
            if (!tableOrderStr.equals("")) {
                intent.putExtra("order", singleOrderStr);
                intent.putExtra("bookmark_id", bookmarkID);
                intent.putExtra("bookmarkType", bookmarkType);

            }
            if (ApplicationParameters.tempResults == null) {
                ApplicationParameters.tempResults = jsonResults;
            }
            //intent.putExtra("json_results", jsonResults.toString());
            intent.putExtra("act_type", actType);
            intent.putExtra("page_num", 0);
            startActivity(intent);
        }
        //test();
    }

    public void sortBy(String text) throws JSONException{
        ArrayList<JSONObject> asList = new ArrayList();
        if (jsonResults == null) {
            jsonResults = new JSONArray();
        }
        for (int i=0; i<jsonResults.length(); i++){
            asList.add(jsonResults.getJSONObject(i));
        }
        final String key = ApplicationParameters.getTableFieldKeyByName(text, ApplicationParameters.tableFields);
        if (sortedBy == key) return;
        sortedBy = key;
        Collections.sort(asList, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = new String();
                String valB = new String();

                try {
                    valA = (String) a.get(key);
                    valB = (String) b.get(key);
                } catch (JSONException e) {
                }

                int comp = valA.compareTo(valB);

                if (comp > 0)
                    return 1;
                if (comp < 0)
                    return -1;
                return 0;
            }
        });
        jsonResults = new JSONArray();
        tempJsonResults = new JSONArray();
        for (int i = 0; i < asList.size(); i++) {
            jsonResults.put(asList.get(i));

        }
        for (int i = maxRow*offset; i < getMinNum(); i++) {
            tempJsonResults.put(asList.get(i - (offset/4)*100));
        }
        offset = 0;
        showProgress(true);
        ll.removeView(results);
        lf.removeView(row);
       // ll.removeView(row);

        /*try {
            setTempJsonResults();
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
        registerInBackground();

    }

    public void sortBy(String text, final int upSort) throws JSONException{
        ArrayList<JSONObject> asList = new ArrayList();
        for (int i=0; i<jsonResults.length(); i++){
            asList.add(jsonResults.getJSONObject(i));
        }
        lastSort = text;
        lastSortOrder = upSort;
        final String key = ApplicationParameters.getTableFieldKeyByName(text, ApplicationParameters.tableFields);
        //if (sortedBy.equals(key)) return;
        //sortedBy = key;
        Collections.sort(asList, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = new String();
                String valB = new String();

                try {
                    valA = (String) a.get(key);
                    valB = (String) b.get(key);
                } catch (JSONException e) {
                }

                int comp = valA.compareTo(valB);

                if ((comp > 0 && upSort != 2) || (comp < 0 && upSort == 2))
                    return 1;
                if ((comp < 0 && upSort != 2) || (comp > 0 && upSort == 2))
                    return -1;
                return 0;
            }
        });
        jsonResults = new JSONArray();
        tempJsonResults = new JSONArray();
        for (int i = 0; i < asList.size(); i++) {
            jsonResults.put(asList.get(i));

        }
        for (int i = maxRow*offset; i < getMinNum(); i++) {
            tempJsonResults.put(asList.get(i - (offset/4)*100));
        }
        offset = 0;
        showProgress(true);
        ll.removeView(results);
        lf.removeView(row);
       // ll.removeView(row);
       /* try {
            setTempJsonResults();
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
        registerInBackground();

    }

    public void refreshTable() {

        showProgress(true);

        tableOrder = ApplicationParameters.chosenSearchResultsTableFields;
        for (int i = 0; i < tableOrder.size(); i++) {
            headers.put(i, 0);
        }
        ll.removeView(results);
        lf.removeView(row);
      //  ll.removeView(row);
        try {
            setTempJsonResults();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        registerInBackground();
    }

    @Override
    public void setParams() {

        super.setParams();
        prev = (Button)findViewById(R.id.prev);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (offset > 0)
                    offset--;
                else return;

                ApplicationParameters.offset = offset;
                if ((offset+1)*maxRow % 100 == 0) {
                    refreshRequest(offset);
                } else {
                    refreshTable();
                }


            }
        });
        next = (Button)findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getMinNum() < ApplicationParameters.currentLength) offset++;
                else return;
                ApplicationParameters.offset = offset;
                if (offset*maxRow % 100 == 0) {
                    refreshRequest(offset);
                } else {
                    refreshTable();
                }
            }
        });
        setPrevNext();
    }
    Button prev;
    Button next;

    @Override
    public void onResume() {
        super.onResume();
        if (ApplicationParameters.changed) {
            ApplicationParameters.changed = false;
            if (bookmarkID < 1)
                refreshTable();
            else
                refreshRequest();
        }
    }

    private void setPrevNext() {
        prev.setVisibility(offset == 0 ? View.GONE : View.VISIBLE);
        next.setVisibility(getMinNum() == ApplicationParameters.currentLength ? View.GONE : View.VISIBLE);
        prev.setText("Показать с " + Integer.toString(maxRow*(offset-1)+1) + " по " + Integer.toString(maxRow*(offset)) + " записи (из " + Integer.toString(ApplicationParameters.currentLength) + ")");
        next.setText("Показать с " + Integer.toString(maxRow*(offset+1)+1) + " по " + Integer.toString((maxRow*(offset+2) > ApplicationParameters.currentLength) ? ApplicationParameters.currentLength : maxRow*(offset+2)) + " записи (из " + Integer.toString(ApplicationParameters.currentLength) + ")");

    }

    private View mProgressView;


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    TableRow row;


    public class CheckToken2 extends AsyncTask<Integer, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(final Integer... args) {
            JSONObject jsonObject = null;
            final String access_token = getPref("access_token");
            final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
            JSONParser parser = new JSONParser();
            jsonObject = parser.getJSONFromUrl("http://"+ApplicationParameters.MAIN_DOMAIN +"/search", new HashMap<String, String>() {{
                put("access_token", access_token);
                put("hs", secure_token);
                put("filter_object", ApplicationParameters.lastSearchString.toString());
                if (args.length > 0) {
                    put("offset", Integer.toString(args[0]));
                }
                if (bookmarkID > 0) {
                    put("is_custom_bookmark", "1");
                    put("bookmark_id", Integer.toString(bookmarkID));
                }
            }}, getApplicationContext());
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            if (res == null) {
                Toast.makeText(SearchResults.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
            } else
            if (res.has("error")) {
                Intent intent = new Intent(SearchResults.this, LoginActivity.class);
                intent.putExtra("login", true);
                startActivity(intent);
                finish();
            } else {
                try {
                    saveCredentials(res.getString("access_token"), "access_token");
                    ApplicationParameters.currentLength = Integer.parseInt(res.getString("count"));
                    showProgress(false);
                    jsonResults = new JSONArray(res.getString("result"));
                    ApplicationParameters.lastResults = new JSONArray();
                    ApplicationParameters.tempResults = new JSONArray();
                    ApplicationParameters.lastResults = jsonResults;
                    if (jsonResults == null) {
                        jsonResults = new JSONArray();
                    }
                    for (int i = 0; i < jsonResults.length(); i++) {
                        ApplicationParameters.tempResults.put(ApplicationParameters.lastResults.get(i));
                    }
                    try {
                        ApplicationParameters.setTrafficDocuments(new JSONArray(res.getString("documents")));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    for (int i = 0; i < tableOrder.size(); i++) {
                        headers.put(i, 0);
                    }
                    refreshTable();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void refreshRequest() {
        if (ApplicationParameters.trafficDocuments != null)
            ApplicationParameters.trafficDocuments.clear();
        ApplicationParameters.tempResults = null;
        ApplicationParameters.lastResults = null;
        System.gc();
        showProgress(true);
        if (bookmarkID < 0 || bookmarkType == 1)
            (new CheckToken2()).execute();
        else
            (new BookmarkToken()).execute();
    }

    public void refreshRequest(int offset) {
        if (ApplicationParameters.trafficDocuments != null)
            ApplicationParameters.trafficDocuments.clear();
        ApplicationParameters.tempResults = null;
        ApplicationParameters.lastResults = null;
        System.gc();
        showProgress(true);
        if (bookmarkID < 0)
            (new CheckToken2()).execute(offset*maxRow);
        else
            (new BookmarkToken()).execute();
    }

    private class BookmarkToken extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(final String... args) {
            JSONObject jsonObject = null;
            final String access_token = getPref("access_token");
            final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
            JSONParser parser = new JSONParser();
            jsonObject = parser.getJSONFromUrl("http://"+ApplicationParameters.MAIN_DOMAIN +"/search/bookmark_search", new HashMap<String, String>() {{
                put("access_token", access_token);
                put("hs", secure_token);
                put("bookmark_id", Integer.toString(bookmarkID));
            }}, getApplicationContext());
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            if (res == null) {
                Toast.makeText(SearchResults.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
            } else
            if (res.has("error")) {
                Intent intent = new Intent(SearchResults.this, LoginActivity.class);
                intent.putExtra("login", true);
                startActivity(intent);
                finish();
            } else {
                try {
                    saveCredentials(res.getString("access_token"), "access_token");
                    showProgress(false);
                    jsonResults = new JSONArray(res.getString("result"));
                    ApplicationParameters.lastResults = new JSONArray();
                    ApplicationParameters.tempResults = new JSONArray();
                    ApplicationParameters.currentLength = Integer.parseInt(res.getString("count"));
                    ApplicationParameters.lastResults = jsonResults;
                    if (jsonResults == null) {
                        jsonResults = new JSONArray();
                    }
                    for (int i = 0; i < jsonResults.length(); i++) {
                        ApplicationParameters.tempResults.put(ApplicationParameters.lastResults.get(i));
                    }
                    ApplicationParameters.setTrafficDocuments(new JSONArray(res.getString("documents")));
                    tableOrder.clear();
                    String order = res.getString("order");
                    if (order != null && !(order.isEmpty())) {
                        String[] orders = order.split("<<<");
                        tableOrderStr = orders[0];
                        singleOrderStr = orders[1];
                        String[] tOrders = tableOrderStr.split(">");
                        for (int i = 0; i < tOrders.length; i++) {
                            tableOrder.add(ApplicationParameters.getTableFieldById(Integer.parseInt(tOrders[i]), ApplicationParameters.tableFields));
                        }
                    } else {
                        tableOrder = ApplicationParameters.chosenSearchResultsTableFields;
                    }
                    if (tableOrder.size() == 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SearchResults.this);
                        builder.setTitle("Предупреждение");
                        builder.setMessage("В свойствах вида таблицы не выбрано ни одного поля");
                        builder.setCancelable(true);
                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() { // Кнопка ОК
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss(); // Отпускает диалоговое окно
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }

                    for (int i = 0; i < tableOrder.size(); i++) {
                        headers.put(i, 0);
                    }
                    refreshTable();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (actType == 0)
            getMenuInflater().inflate(R.menu.search_results, menu);
        return true;
    }

    private JSONArray createTestResults() {
        JSONObject obj0 = new JSONObject() {
            {
                try {
                    put("key1", "key1val1");
                    put("key2", "key2val1");
                    put("key3", "key3val1");
                    put("key4", "key4val1");
                    put("key5", "key5val1");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        JSONObject obj1 = new JSONObject() {
            {
                try {
                    put("key1", "ke1val1");
                    put("key2", "ke2val1");
                    put("key3", "ke3val1");
                    put("key4", "keval1");
                    put("key5", "key5val1");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        JSONObject obj2 = new JSONObject() {
            {
                try {
                    put("key1", "key1val1");
                    put("key2", "keval1");
                    put("key3", "key3val1");
                    put("key4", "key4val1");
                    put("key5", "key51");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        JSONObject obj3 = new JSONObject() {
            {
                try {
                    put("key1", "key1val1");
                    put("key2", "key2val1");
                    put("key3", "key3val1");
                    put("key4", "key4vadasl1");
                    put("key5", "key5dsaadval1");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        JSONObject obj4 = new JSONObject() {
            {
                try {
                    put("key1", "kedsay1val1");
                    put("key2", "key2vdal1");
                    put("key3", "key3vadsal1");
                    put("key4", "key4val1");
                    put("key5", "key5val1");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(obj0);
        jsonArray.put(obj1);
        jsonArray.put(obj2);
        jsonArray.put(obj3);
        jsonArray.put(obj4);
        return jsonArray;
    }

    @Override
    public void onBackPressed() {
        if (ApplicationParameters.trafficDocuments != null)
            ApplicationParameters.trafficDocuments.clear();
        ApplicationParameters.tempResults = null;
        ApplicationParameters.lastResults = null;
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public class HeaderView extends TextView {


        public boolean up = true;
        public HeaderView(Context context) {
            super(context);
        }
    }

    public HashMap<Integer, Integer> headers = new HashMap<Integer, Integer>();

    protected void formTable(JSONArray jsonArray) {
        ViewGroup.LayoutParams params = results.getLayoutParams();
        results = new TableLayout(SearchResults.this);
        results.setLayoutParams(params);
        results.setStretchAllColumns(true);
        if (jsonArray == null) {
            jsonArray = new JSONArray();
        }

        ViewGroup.LayoutParams params1 = row.getLayoutParams();
        row = new TableRow(SearchResults.this);
        row.setLayoutParams(params1);
        row.setBackgroundColor(getResources().getColor(R.color.even_color));
        row.setLayoutParams(new TableLayout.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        Typeface typeface = Typeface.createFromAsset(getAssets(), "Roboto-Regular_0.ttf");
        if (jsonArray.length() > 0) {
            Iterator<String> keys = null;
            try {
                keys = jsonArray.getJSONObject(0).keys();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ArrayList<String> copy = new ArrayList<String>();
            while (keys.hasNext())
                copy.add(keys.next());
            for (int i = 0; i < tableOrder.size(); i++) {
                for (int k = 0; k < copy.size(); k++) {
                    String key = copy.get(k);
                    try {
                        if (tableOrder.get(i).getName().equals(key)) {

                            TextView tv = new TextView(this);
                            tv.setClickable(true);

                            tv.setTypeface(typeface);
                            final int finalI = i;
                            tv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (ApplicationParameters.isTBB && ApplicationParameters.currentLength > 100) return;
                                    try {
                                        sortBy(((TextView) view).getText().toString(), headers.get(finalI));
                                        for (int k = 0; k < tableOrder.size(); k++) {
                                            if (k != finalI)
                                                headers.put(k, 0);
                                        }
                                        headers.put(finalI, 3 - Math.max(headers.get(finalI), 1));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            try {
                                tv.setMaxLines(1);
                                tv.setSingleLine(true);
                                tv.setEllipsize(TextUtils.TruncateAt.END);
                                tv.setLayoutParams(new TableRow.LayoutParams(
                                        getWindowManager().getDefaultDisplay().getWidth() / 3,
                                        ViewGroup.LayoutParams.WRAP_CONTENT, 1f) {{
                                    setMargins(3, 3, 3, 3);
                                }});
                                tv.setTextSize(15);
                                tv.setText(tableOrder.get(i).getText());
                                tv.setPadding(5, 5, 5, 5);
                                tv.setBackgroundColor(getResources().getColor(R.color.even_color));

                                if (headers.get(i) != 0) {
                                    tv.setTypeface(null, Typeface.BOLD);
                                    tv.setText(((headers.get(i) == 2) ? "▲ " : "▼ ") + tv.getText());
                                }
                                tv.setTextColor(getResources().getColor(R.color.dark_text_color));
                                row.addView(tv);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (NullPointerException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        } else {
            for (int i = 0; i < tableOrder.size(); i++) {

                TextView tv = new TextView(this);
                try {
                    tv.setMaxLines(1);
                    tv.setSingleLine(true);
                    tv.setEllipsize(TextUtils.TruncateAt.END);
                    tv.setTypeface(typeface);
                    tv.setLayoutParams(new TableRow.LayoutParams(
                            getWindowManager().getDefaultDisplay().getWidth()/3,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 1f) {{
                        setMargins(3, 3, 3, 3);
                    }});
                    tv.setTextSize(15);
                    tv.setText(tableOrder.get(i).getText());
                    tv.setPadding(5, 5, 5, 5);
                    tv.setBackgroundColor(getResources().getColor(R.color.even_color));

                    tv.setTextColor(getResources().getColor(R.color.dark_text_color));

                    row.addView(tv);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        int[] colors = new int[] { 0xffffffff, 0xffededee };
       // results.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                TableRow row = createTableRow(jsonArray.getJSONObject(i),i);
                int colorPos = i % colors.length;
                row.setBackgroundColor(colors[colorPos]);
                results.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    protected TableRow createTableRow(JSONObject jsonObject, final int len) {
        TableRow row = new TableRow(this);
        row.setBackgroundDrawable(getResources().getDrawable(R.drawable.table_cell));

        row.setLayoutParams(new TableLayout.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.MATCH_PARENT));
        row.setClickable(true);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchResults.this, ViewPagerSearchActivity.class);
                if (!tableOrderStr.equals("")) {
                    intent.putExtra("order", singleOrderStr);
                    intent.putExtra("bookmark_id", bookmarkID);
                    intent.putExtra("bookmarkType", bookmarkType);

                }
                if (ApplicationParameters.tempResults == null) {
                    ApplicationParameters.tempResults = jsonResults;
                }
                //intent.putExtra("json_results", jsonResults.toString());
                intent.putExtra("act_type", actType);
                intent.putExtra("page_num", len + maxRow*(offset % 4));
                startActivity(intent);
            }
        });
        Iterator<String> keys = jsonObject.keys();
        ArrayList<String> copy = new ArrayList<String>();
        while (keys.hasNext())
            copy.add(keys.next());

        for (int i = 0; i < tableOrder.size(); i++) {
            for (int k = 0; k < copy.size(); k++) {
                String key = copy.get(k);
                try {
                    if (tableOrder.get(i).getName().equals(key)) {
                        TextView tv = new TextView(this);
                        try {
                            tv.setTypeface(typeface);
                            tv.setMaxLines(1);
                            tv.setSingleLine(true);
                            tv.setEllipsize(TextUtils.TruncateAt.END);
                            tv.setLayoutParams(new TableRow.LayoutParams(
                                    getWindowManager().getDefaultDisplay().getWidth() / 3,
                                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f) {{
                                setMargins(3, 3, 3, 3);
                            }});
                            //tv.setBackgroundDrawable(getResources().getDrawable(R.drawable.table_cell));

                            tv.setTextSize(15);
                            if (ApplicationParameters.getFieldType(key, ApplicationParameters.tableFields).equals("date")) {
                                tv.setText(getDateFromTimestamp(jsonObject.getString(key)));
                            } else {
                                if (key.equals("ID_STATUS_EX2")) {
                                    jsonObject.put("ID_STATUS_EX2", new String(jsonObject.getString(key).getBytes("cp1251")));
                                }
                                tv.setText(jsonObject.getString(key));
                            }

                            tv.setPadding(5, 5, 5, 5);
                            tv.setTextColor(getResources().getColor(R.color.dark_text_color));
                            row.addView(tv);
                        } catch (Exception e) {
                           // e.printStackTrace();
                        }
                    }
                } catch (NullPointerException e2) {
                   // e2.printStackTrace();
                }
            }
        }

        return row;
    }


    public String getDateFromTimestamp(String stamp) {
        if (stamp.isEmpty() || stamp == null) return "";
        long unixSeconds = new Long(stamp);
        Date date = new Date(unixSeconds*1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy"); // the format of your date
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+4")); // give a timezone reference for formating (see comment at the bottom
        /*SimpleDateFormat sdf2 = new SimpleDateFormat("dd.MM.yyyy"); // the format of your date
        sdf2.setTimeZone(TimeZone.getTimeZone("GMT+4"));*/
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

   /* @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            ApplicationParameters.activity = this;
            Intent intent = new Intent(this, PushSettingsActivityVer2.class);
            intent.putExtra("tab", 1);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/
}
