package com.logistic.paperrose.mttp.oldversion.results;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

public class BaseReportsSearchResults extends BaseLogisticActivity {
    protected TableLayout results;
    public JSONArray jsonResults;
    protected String jsonString;
    protected JSONArray tempJsonResults = new JSONArray();
    protected LinearLayout ll;
    protected LinearLayout lf;
    protected String sortedBy = "";
    int maxRow = 25;



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ApplicationParameters.activity = this;
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                formTable(tempJsonResults);
                return null;
            }

            @Override
            protected void onPostExecute(Void msg) {
                View tv = findViewById(R.id.emptyRecords);
                if (tempJsonResults == null) {
                    tempJsonResults = new JSONArray();
                }
                if (tempJsonResults.length() == 0) {

                    tv.setVisibility(View.VISIBLE);
                } else {
                    tv.setVisibility(View.GONE);
                }
                findViewById(R.id.fixedHeaders).setVisibility(View.GONE);
                try {
                    lf.addView(row);
                } catch (Exception e) {
                    ((LinearLayout)row.getParent()).removeView(row);
                    lf.addView(row);
                }
                ll.addView(results, 0);
                setPrevNext();
                showProgress(false);
            }
        }.execute(null, null, null);
    }
    public int offset = 0;

    private int getMinNum() {
        return (maxRow*(offset+1) > ApplicationParameters.currentLength) ? ApplicationParameters.currentLength : maxRow*(offset+1);
    }

    private void setTempJsonResults() throws JSONException {
        tempJsonResults = new JSONArray();
        for (int i = maxRow*offset; i < getMinNum(); i++) {
            tempJsonResults.put(ApplicationParameters.lastResults.getJSONObject(i - (offset/4)*100));
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLayoutId(R.layout.activity_search_results);
        jsonResults = ApplicationParameters.lastResults;
        if (ApplicationParameters.chosenDeclarationReportsFields.size() == 0) {
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
        registerInBackground();
        if (jsonResults == null) {
            jsonResults = new JSONArray();
        }
        if (jsonResults.length() == 1) {
            Intent intentw = new Intent(BaseReportsSearchResults.this, BaseReportsViewPagerSearchActivity.class);
            ApplicationParameters.activity = BaseReportsSearchResults.this;
            // intent.putExtra("json_results", jsonResults.toString());
            intentw.putExtra("page_num", 0);
            intentw.putExtra("type", ApplicationParameters.decType);
            startActivity(intentw);
        }
        //test();
    }

    @Override
    public void refreshRequest() {
        showProgress(true);
        (new CheckToken3()).execute(ApplicationParameters.decType);
    }


    private class CheckToken3 extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... args) {
            JSONObject jsonObject = null;
            String cc = "";
            if (args.length > 1) cc = args[1];
            try {

                jsonObject = checkServerCredentials2(args[0], cc, Integer.toString(offset*maxRow));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            showProgress(false);
            if (res == null) {
                Toast.makeText(BaseReportsSearchResults.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
            } else

            if (res.has("error")) {
                try {
                    if (res.getString("error").equals("Results count is too big")) {
                        return;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (res.getInt("status_code") == 401) {
                        Intent intent = new Intent(BaseReportsSearchResults.this, LoginActivity.class);
                        intent.putExtra("login", true);
                        startActivity(intent);
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    saveCredentials(res.getString("access_token"), "access_token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    ApplicationParameters.lastResults = new JSONArray();
                    ApplicationParameters.tempResults = new JSONArray();
                    ApplicationParameters.currentLength = Integer.parseInt(res.getString("count"));
                    JSONArray tmp = new JSONArray(res.getString("result"));
                    ApplicationParameters.lastResults = tmp;
                    if (tmp == null) {
                        tmp = new JSONArray();
                    }
                    for (int i = 0; i < tmp.length(); i++) {
                        ApplicationParameters.tempResults.put(ApplicationParameters.lastResults.get(i));
                    }
                    try {
                        ApplicationParameters.setTrafficDocuments(new JSONArray(res.getString("documents")));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (ApplicationParameters.chosenDeclarationReportsFields.size() == 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(BaseReportsSearchResults.this);
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
                    refreshTable();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //return true;
            }
        }
    }


    public void sortBy(String text) throws JSONException{
        ArrayList<JSONObject> asList = new ArrayList();
        if (jsonResults == null) {
            jsonResults = new JSONArray();
        }
        for (int i=0; i<jsonResults.length(); i++){
            asList.add(jsonResults.getJSONObject(i));
        }
        final String key = ApplicationParameters.getTableFieldKeyByName(text, ApplicationParameters.declarationFields);
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
        for (int i = 0; i < asList.size(); i++) {
            jsonResults.put(asList.get(i));
        }
        offset = 0;
        showProgress(true);
        ll.removeView(results);
        lf.removeView(row);
        try {
            setTempJsonResults();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        registerInBackground();

    }

    public void refreshTable() {

        showProgress(true);

        ll.removeView(results);
        lf.removeView(row);
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
                    refreshRequest();
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
                    refreshRequest();
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
            refreshTable();
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
        if (ApplicationParameters.eds != null)
            ApplicationParameters.eds.clear();
        ApplicationParameters.tempResults = null;
        ApplicationParameters.lastResults = null;
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    protected void formTable(JSONArray jsonArray) {
        if (jsonArray == null) {
            jsonArray = new JSONArray();
        }
        ViewGroup.LayoutParams params = results.getLayoutParams();
        results = new TableLayout(BaseReportsSearchResults.this);
        results.setLayoutParams(params);
        int[] colors = new int[] { 0xffffffff, 0xffededee };
        results.setStretchAllColumns(true);
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                TableRow row = createTableRow(jsonArray.getJSONObject(i),i);
                int colorPos = i % colors.length;
                row.setBackgroundColor(colors[colorPos]);
                results.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        ViewGroup.LayoutParams params1 = row.getLayoutParams();
        row = new TableRow(BaseReportsSearchResults.this);
        row.setLayoutParams(params1);
        row.setBackgroundColor(getResources().getColor(R.color.secondary_color));
        row.setLayoutParams(new TableLayout.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));

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

            for (int i = 0; i < ApplicationParameters.chosenDeclarationReportsFields.size(); i++) {
                for (int k = 0; k < copy.size(); k++) {
                    String key = copy.get(k);
                    if (ApplicationParameters.chosenDeclarationReportsFields.get(i).getName().equals(key)) {

                        TextView tv = new TextView(this);
                        tv.setMaxLines(1);
                        tv.setSingleLine(true);
                        tv.setClickable(true);
                        tv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    sortBy(((TextView) view).getText().toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        try {
                            tv.setMaxLines(3);
                            tv.setLayoutParams(new TableRow.LayoutParams(
                                    getWindowManager().getDefaultDisplay().getWidth()/3,
                                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f) {{
                                setMargins(3, 3, 3, 3);
                            }});
                            tv.setTextSize(15);
                            tv.setText(ApplicationParameters.chosenDeclarationReportsFields.get(i).getText());
                            tv.setPadding(5, 5, 5, 5);
                            tv.setBackgroundColor(getResources().getColor(R.color.secondary_color));
                            tv.setTextColor(Color.parseColor("#ffffff"));
                            row.addView(tv);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < ApplicationParameters.chosenDeclarationReportsFields.size(); i++) {

                TextView tv = new TextView(this);
                try {
                    tv.setMaxLines(1);
                    tv.setSingleLine(true);
                    tv.setLayoutParams(new TableRow.LayoutParams(
                            getWindowManager().getDefaultDisplay().getWidth()/3,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 1f) {{
                        setMargins(3, 3, 3, 3);
                    }});
                    tv.setTextSize(15);
                    tv.setText(ApplicationParameters.chosenDeclarationReportsFields.get(i).getText());
                    tv.setPadding(5, 5, 5, 5);
                    tv.setBackgroundColor(getResources().getColor(R.color.secondary_color));
                    tv.setTextColor(Color.parseColor("#ffffff"));
                    row.addView(tv);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    protected TableRow createTableRow(JSONObject jsonObject, final int len) {
        TableRow row = new TableRow(this);
        row.setBackgroundDrawable(getResources().getDrawable(R.drawable.table_cell));

        row.setLayoutParams(new TableLayout.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        row.setClickable(true);
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentw = new Intent(BaseReportsSearchResults.this, BaseReportsViewPagerSearchActivity.class);
                ApplicationParameters.activity = BaseReportsSearchResults.this;
               // intent.putExtra("json_results", jsonResults.toString());
                intentw.putExtra("page_num", len + maxRow*(offset % 4));
                intentw.putExtra("type", ApplicationParameters.decType);
                startActivity(intentw);
            }
        });
        Iterator<String> keys = jsonObject.keys();
        ArrayList<String> copy = new ArrayList<String>();
        while (keys.hasNext())
            copy.add(keys.next());

        for (int i = 0; i < ApplicationParameters.chosenDeclarationReportsFields.size(); i++) {
            for (int k = 0; k < copy.size(); k++) {
                String key = copy.get(k);
                if (ApplicationParameters.chosenDeclarationReportsFields.get(i).getName().equals(key)) {
                    TextView tv = new TextView(this);
                    try {
                        tv.setMaxLines(1);
                        tv.setSingleLine(true);
                        tv.setLayoutParams(new TableRow.LayoutParams(
                                getWindowManager().getDefaultDisplay().getWidth()/3,
                                ViewGroup.LayoutParams.WRAP_CONTENT, 1f) {{
                            setMargins(3, 3, 3, 3);
                        }});
                        // tv.setText("ура");
                        tv.setTextSize(15);
                        if (ApplicationParameters.getFieldType(key, ApplicationParameters.declarationFields).equals("date")) {
                            tv.setText(getDateFromTimestamp(jsonObject.getString(key)));
                        } else {
                            tv.setText(jsonObject.getString(key));
                        }

                        tv.setPadding(5, 5, 5, 5);
                        tv.setTextColor(getResources().getColor(R.color.secondary_color));
                        row.addView(tv);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base_reports_search_results, menu);
        return true;
    }


    /*@Override
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
