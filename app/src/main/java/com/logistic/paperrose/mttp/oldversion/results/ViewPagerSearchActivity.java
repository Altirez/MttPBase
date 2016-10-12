package com.logistic.paperrose.mttp.oldversion.results;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Toast;

import com.logistic.paperrose.mttp.oldversion.BaseLogisticActivity;
import com.logistic.paperrose.mttp.oldversion.LoginActivity;
import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.newsearch.DictionaryAutoCompleteAdapter;
import com.logistic.paperrose.mttp.oldversion.newsearch.SearchPageAdapter;
import com.logistic.paperrose.mttp.oldversion.search.DelayedFilteringField;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;
import com.logistic.paperrose.mttp.oldversion.utils.ExtendedEditText;
import com.logistic.paperrose.mttp.oldversion.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

public class ViewPagerSearchActivity extends BaseLogisticActivity {

    List<View> pages = new ArrayList<View>();
    ViewPager viewPager;
    LayoutInflater inflater;
    public int bookmarkType;
    public JSONArray jsonResults;
    public SearchResults parentActivity = null;
    public int lastNum;
    public int bookmarkID = -1;
    public int actType;
    public int currentPageNum;

    public int status;

    public ArrayList<TripleTableField> singleFields = new ArrayList<TripleTableField>();
    SearchViewPagerAdapter pagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLayoutId(R.layout.activity_view_pager_search);
        String order = getIntent().getStringExtra("order");
        actType = getIntent().getIntExtra("act_type", 0);
        bookmarkID = getIntent().getIntExtra("bookmark_id", -1);
        bookmarkType = getIntent().getIntExtra("bookmarkType", 0);
        if (order != null && !( order.equals("") || order.isEmpty())) {
            String[] tOrders = order.split(">");
            for (int i = 0; i < tOrders.length; i++) {
                singleFields.add(ApplicationParameters.getTableFieldById(Integer.parseInt(tOrders[i]), ApplicationParameters.tableFields));
            }
        } else {
            singleFields = ApplicationParameters.chosenSearchResultSingleFields;
        }
        if (singleFields.size() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ViewPagerSearchActivity.this);
            builder.setTitle("Предупреждение");
            builder.setMessage("В свойствах вида записи не выбрано ни одного поля");
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

        super.onCreate(savedInstanceState);
        inflater = LayoutInflater.from(ViewPagerSearchActivity.this);
        viewPager = (ViewPager)findViewById(R.id.viewPager);
        try {
            jsonResults = ApplicationParameters.tempResults;
            if (jsonResults == null) {
                jsonResults = ApplicationParameters.lastResults;
            }
            if (jsonResults == null) {
                refreshRequest();
                return;
            }
            for (int i = 0; i < jsonResults.length(); i++) {
                createPage(jsonResults.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pagerAdapter = new SearchViewPagerAdapter(pages);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                if (adapters.get(currentPageNum).isChanged()) {
                    showDialog(1);
                } else {
                    currentPageNum = i;
                }

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        lastNum = getIntent().getIntExtra("page_num", 1);
        currentPageNum = lastNum;
        viewPager.setCurrentItem(lastNum);
        parentActivity = (SearchResults)ApplicationParameters.activity;
        ExtendedEditText et = (ExtendedEditText) findViewById(R.id.search);
        et.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapters.get(currentPageNum).getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (ApplicationParameters.changed && bookmarkID > 0) {
            ApplicationParameters.changed = false;
            refreshRequest();
        }
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
                Toast.makeText(ViewPagerSearchActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
            } else
            if (res.has("error")) {
                try {
                    if (res.getInt("status_code") == 401) {
                        Intent intent = new Intent(ViewPagerSearchActivity.this, LoginActivity.class);
                        intent.putExtra("login", true);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(ViewPagerSearchActivity.this, LoginActivity.class);
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
                    showProgress(false);
                    singleFields.clear();
                    String order = res.getString("order").split("<<<")[1];
                    if (order != null && !( order.equals("") || order.isEmpty())) {
                        String[] tOrders = order.split(">");
                        for (int i = 0; i < tOrders.length; i++) {
                            singleFields.add(ApplicationParameters.getTableFieldById(Integer.parseInt(tOrders[i]), ApplicationParameters.tableFields));
                        }
                    } else {
                        singleFields = ApplicationParameters.chosenSearchResultSingleFields;
                    }
                    if (singleFields.size() == 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ViewPagerSearchActivity.this);
                        builder.setTitle("Предупреждение");
                        builder.setMessage("В свойствах вида записи не выбрано ни одного поля");
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
                    refresh(new JSONArray(res.getString("result")));
                    if (parentActivity != null)
                        parentActivity.jsonResults = new JSONArray(res.getString("result"));

                } catch (Exception e) {
                    e.printStackTrace();
                }

                //return true;
            }
        }
    }



    public class CheckToken2 extends AsyncTask<Pair<String, HashMap<String, String>>, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Pair<String, HashMap<String, String>>... args) {
            JSONObject jsonObject = null;
            final String access_token = getPref("access_token");
            final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
            JSONParser parser = new JSONParser();
            jsonObject = parser.getJSONFromUrl("http://"+ApplicationParameters.MAIN_DOMAIN +"/search", new HashMap<String, String>() {{
                put("access_token", access_token);
                put("hs", secure_token);
                put("filter_object", ApplicationParameters.lastSearchString.toString());
            }}, getApplicationContext());
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            if (res == null) {
                Toast.makeText(ViewPagerSearchActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
            } else
            if (res.has("error")) {
                Intent intent = new Intent(ViewPagerSearchActivity.this, LoginActivity.class);
                intent.putExtra("login", true);
                startActivity(intent);
                finish();
            } else {
                try {
                    saveCredentials(res.getString("access_token"), "access_token");
                    showProgress(false);
                    ApplicationParameters.currentLength = Integer.parseInt(res.getString("count"));
                    refresh(new JSONArray(res.getString("result")));
                    if (parentActivity != null)
                        parentActivity.jsonResults = new JSONArray(res.getString("result"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //return true;
            }
        }
    }


    @Override
    public void refreshRequest() {
        showProgress(true);
        if (bookmarkID < 0)
            (new CheckToken2()).execute();
        else
            (new BookmarkToken()).execute();
    }

    public void refresh(JSONArray results) {
        pages.clear();
        jsonResults = results;
        try {
            for (int i = 0; i < jsonResults.length(); i++) {
                createPage(jsonResults.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SearchViewPagerAdapter pagerAdapter = new SearchViewPagerAdapter(pages);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(lastNum);
        pagerAdapter.notifyDataSetChanged();
    }


    SearchPageAdapter adapter;
    public void createPage(JSONObject obj) throws JSONException {
        View page = inflater.inflate(R.layout.page_view, null);
        ListView lv = (ListView) page.findViewById(R.id.searchResult);
        final Button bt = (Button)page.findViewById(R.id.saveChangesButton);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bt.setVisibility(View.GONE);
                try {
                    ApplicationParameters.lastResults.put(currentPageNum, ApplicationParameters.tempResults.getJSONObject(currentPageNum));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                adapters.get(viewPager.getCurrentItem()).isChanged(false);
                currentPageNum = viewPager.getCurrentItem();
            }
        });
        Iterator<String> keys = obj.keys();
        ArrayList<TripleTableField> objS = new ArrayList<TripleTableField>();
        ArrayList<String> copy = new ArrayList<String>();
        while (keys.hasNext())
            copy.add(keys.next());
        for (int i = 0; i < singleFields.size(); i++) {
            for (int k = 0; k < copy.size(); k++) {
                String key = copy.get(k);
                if (singleFields.get(i) != null && singleFields.get(i).getName().equals(key)  && !obj.getString(key).isEmpty()) {
                    objS.add(
                            new TripleTableField(
                                    ApplicationParameters.getTableFieldNameByKey(key, ApplicationParameters.tableFields),
                                    obj.getString(key),
                                    ApplicationParameters.getFieldType(key, ApplicationParameters.tableFields)
                            )
                    );
                }
            }
        }
        adapter  = new SearchPageAdapter(ViewPagerSearchActivity.this, R.layout.search_field_row, objS);
        adapter.pageID = Integer.parseInt(obj.getString("RECORD_ID"));
        try {
            adapter.statusID = Integer.parseInt(obj.getString("TRAFFIC_STATUS_ID"));
        } catch (Exception e) {

        }

        Button bt2 = (Button)page.findViewById(R.id.docsButton);
        bt2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ViewPagerSearchActivity.this, DownloadDocuments.class);
                    intent.putExtra("number", Integer.toString(adapter.pageID));
                    intent.putExtra("table", "LIST_TRAFFIC");
                    startActivity(intent);
                    //finish();
                }
            }

        );

        Button bt3 = (Button)page.findViewById(R.id.changeStatus);
        bt3.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        int statusID = adapter.statusID;
                        Context ctx = ViewPagerSearchActivity.this;

                        if (statusID == 1) {
                            final DelayedFilteringField input = new DelayedFilteringField(ctx);
                            final TripleTableField[] book = new TripleTableField[1];
                            input.setThreshold(1);
                            input.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                                    book[0] = (TripleTableField) adapterView.getItemAtPosition(position);
                                    input.setText(book[0].getText());
                                }
                            });

                            DictionaryAutoCompleteAdapter autoCompleteAdapter = new DictionaryAutoCompleteAdapter(getApplicationContext(), (new TripleTableField("ID_LIST_USERS_DECL", "SURNAME", "LIST_USERS")));
                            input.setAdapter(autoCompleteAdapter);

                            new AlertDialog.Builder(ctx)
                                    .setTitle("Изменение статуса")
                                    .setMessage("Выберите декларанта")
                                    .setView(input)
                                    .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            if (book[0] != null)
                                                (new UpdateStatusRequest()).execute(Integer.toString(adapters.get(currentPageNum).pageID), book[0].getName());
                                        }
                                    }).setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                    // Do nothing.
                                        }
                                    }).show();
                        } else if (statusID < 7) {
                            new AlertDialog.Builder(ctx)
                                    .setTitle("Изменение статуса")
                                    .setMessage("Вы уверены, что хотите изменить статус груза?")
                                    .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            (new UpdateStatusRequest()).execute(Integer.toString(adapters.get(currentPageNum).pageID), "0");
                                        }
                                    }).setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                    // Do nothing.
                                        }
                                    }).show();
                        }

                    }
                }
        );

        lv.setAdapter(adapter);
        registerForContextMenu(lv);
        adapters.add(adapter);

        pages.add(page);
    }

    public String getPref(String key) {
        return getSharedPreferences("UserPreference", 0).getString(key, "123");
    }


    public JSONObject checkServerCredentials() throws JSONException {
        final String access_token = getPref("access_token");
        final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
        JSONParser parser = new JSONParser();
        JSONObject obj = parser.getJSONFromUrl("http://"+ApplicationParameters.MAIN_DOMAIN +"/edit", new HashMap<String, String>() {{
            put("access_token", access_token);
            put("hs", secure_token);
            put("edit_fields", getJSONParameters());
        }}, getApplicationContext());
        return obj;
    }


    private class UpdateStatusRequest extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(final String... args) {
            JSONObject jsonObject = null;
            final String access_token = getPref("access_token");
            final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
            JSONParser parser = new JSONParser();
            JSONObject obj = parser.getJSONFromUrl("http://"+ApplicationParameters.MAIN_DOMAIN +"/change_status", new HashMap<String, String>() {{
                put("access_token", access_token);
                put("hs", secure_token);
                put("record_id", args[0]);
                put("id_user", args[1]);
            }}, getApplicationContext());
            return obj;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            if (res == null) {
                Toast.makeText(ViewPagerSearchActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
            } else {

                if (res.has("error")) {
                    Intent intent = new Intent(ViewPagerSearchActivity.this, LoginActivity.class);
                    intent.putExtra("login", true);
                    startActivity(intent);
                    finish();
                } else {
                    adapter.statusID += 1;
                    try {
                        Toast.makeText(ViewPagerSearchActivity.this, "Статус контейнера успешно изменен. Новый статус '" + res.getString("status_name") + "'", Toast.LENGTH_LONG).show();
                        TripleTableField old_status_field = ApplicationParameters.getTableFieldByKey(res.getString("old_status_id"), ApplicationParameters.status_counts);
                        TripleTableField new_status_field = ApplicationParameters.getTableFieldByKey(res.getString("new_status_id"), ApplicationParameters.status_counts);
                        old_status_field.setText(Integer.toString(Integer.parseInt(old_status_field.getText()) - 1));
                        new_status_field.setText(Integer.toString(Integer.parseInt(new_status_field.getText()) + 1));
                        String new_status = res.getString("new_status_id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private class CheckToken extends AsyncTask<Pair<String, HashMap<String, String>>, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Pair<String, HashMap<String, String>>... args) {
            JSONObject jsonObject = null;
            try {
                jsonObject = checkServerCredentials();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            if (res == null) {
                Toast.makeText(ViewPagerSearchActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
            } else {
                changedDictFields.clear();
                if (res.has("error")) {
                    Intent intent = new Intent(ViewPagerSearchActivity.this, LoginActivity.class);
                    intent.putExtra("login", true);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        saveCredentials(res.getString("access_token"), "access_token");
                        ApplicationParameters.lastResults.put(currentPageNum, ApplicationParameters.tempResults.getJSONObject(currentPageNum));
                        adapters.get(currentPageNum).isChanged(false);
                        currentPageNum = viewPager.getCurrentItem();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    ArrayList<TripleTableField> changedDictFields = new ArrayList<TripleTableField>();

    public void addChangedDictField(TripleTableField field) {
        for (TripleTableField ff : changedDictFields) {
            if (ff.getName().equals(field.getName())) {
                ff.setText(field.getText());
                return;
            }
        }
        changedDictFields.add(field);
    }

    public String getChangedDictFieldID(String name) {
        for (TripleTableField ff : changedDictFields) {
            if (ff.getName().equals(name)) {
                return ff.getText();
            }
        }
        return null;
    }

    public String getJSONParameters() {
        JSONArray arr = new JSONArray();
        for (int i = 0; i < singleFields.size(); i++) {
            TripleTableField cc = singleFields.get(i);
            JSONObject obj = new JSONObject();
            try {
                if (adapters.get(currentPageNum).getItem(i).getType().equals("dict") ) {
                    String key = ApplicationParameters.getTableFieldKeyByName(adapters.get(currentPageNum).getItem(i).getName(), ApplicationParameters.tableFields);
                    String val = getChangedDictFieldID(key);
                    if (val != null) {
                        obj.put("field_name", ApplicationParameters.getTableFieldByKey(key, ApplicationParameters.dictionaries).additional);
                        obj.put("field_type", adapters.get(currentPageNum).getItem(i).getType());
                        obj.put("field_value", val);
                        arr.put(obj);
                    }
                } else if (adapters.get(currentPageNum).getItem(i).getType().equals("date")) {


                    obj.put("field_name", ApplicationParameters.getTableFieldKeyByName(adapters.get(currentPageNum).getItem(i).getName(), ApplicationParameters.tableFields));
                    obj.put("field_type", adapters.get(currentPageNum).getItem(i).getType());
                    String str =  getDateFromTimestamp(adapters.get(currentPageNum).getItem(i).getText());
                    obj.put("field_value", str);
                    if (!str.isEmpty())
                        arr.put(obj);
                } else {


                    obj.put("field_name", ApplicationParameters.getTableFieldKeyByName(adapters.get(currentPageNum).getItem(i).getName(), ApplicationParameters.tableFields));
                    obj.put("field_type", adapters.get(currentPageNum).getItem(i).getType());
                    obj.put("field_value", adapters.get(currentPageNum).getItem(i).getText());
                    arr.put(obj);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        JSONObject res = new JSONObject();
        try {
            res.put("row_id", adapters.get(currentPageNum).pageID.toString());
            res.put("fields", arr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApplicationParameters.lastSearchString = res;
        return res.toString();
    }


    public void setPage(JSONObject obj, int index) throws JSONException {
        View page = inflater.inflate(R.layout.page_view, null);
        ListView lv = (ListView) page.findViewById(R.id.searchResult);
        final Button bt = (Button)page.findViewById(R.id.saveChangesButton);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bt.setVisibility(View.GONE);
            }
        });
        Iterator<String> keys = obj.keys();
        ArrayList<TripleTableField> objS = new ArrayList<TripleTableField>();
        ArrayList<String> copy = new ArrayList<String>();
        while (keys.hasNext())
            copy.add(keys.next());
        for (int i = 0; i < singleFields.size(); i++) {
            for (int k = 0; k < copy.size(); k++) {
                String key = copy.get(k);
                if (singleFields.get(i).getName().equals(key)  && !obj.getString(key).isEmpty()) {
                    objS.add(
                            new TripleTableField(
                                    ApplicationParameters.getTableFieldNameByKey(key, ApplicationParameters.tableFields),
                                    obj.getString(key),
                                    ApplicationParameters.getFieldType(key, ApplicationParameters.tableFields)
                            )
                    );
                }
            }
        }
        SearchPageAdapter adapter = new SearchPageAdapter(ViewPagerSearchActivity.this, R.layout.search_field_row, objS);
        lv.setAdapter(adapter);
        currentPageNum = viewPager.getCurrentItem();
        registerForContextMenu(lv);
        adapters.set(index, adapter);
        pages.set(index, page);
        pagerAdapter = new SearchViewPagerAdapter(pages);
        pagerAdapter.notifyDataSetChanged();
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(currentPageNum);
        //viewPager.se

    }


    ArrayList<SearchPageAdapter> adapters = new ArrayList<SearchPageAdapter>();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (actType == 0)
            getMenuInflater().inflate(R.menu.view_pager_search, menu);
        return true;
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            refreshRequest();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

    protected Dialog onCreateDialog(int id) {
        if (id == 1) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            // заголовок
            adb.setTitle("Предупреждение");
            // сообщение
            adb.setMessage("Сохранить изменения в записи?");
            // иконка
            adb.setIcon(android.R.drawable.ic_dialog_info);
            // кнопка положительного ответа
            adb.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                        (new CheckToken()).execute();
                }
            });
            adb.setNegativeButton("Не сохранять", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        ApplicationParameters.tempResults.put(currentPageNum, ApplicationParameters.lastResults.getJSONObject(currentPageNum));
                        jsonResults = ApplicationParameters.tempResults;
                        adapters.get(currentPageNum).isChanged(false);
                        setPage(jsonResults.getJSONObject(currentPageNum), currentPageNum);
                        changedDictFields.clear();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            // кнопка отрицательного ответа
            adb.setNeutralButton("Отмена", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    viewPager.setCurrentItem(currentPageNum);
                }
            });
            // кнопка нейтрального ответа
            // создаем диалог
            return adb.create();
        }
        return super.onCreateDialog(id);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
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
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info;

        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            return false;
        }

        switch (item.getItemId()) {
            case R.id.apply:
                final DelayedFilteringField input = new DelayedFilteringField(this);
                final TripleTableField field = adapters.get(viewPager.getCurrentItem()).getItem(info.position);

                if (field.getType().equals("date")) {
                    final Calendar myCalendar = Calendar.getInstance();
                    long unixSeconds;
                    if (field.getText().isEmpty()) {
                        unixSeconds = System.currentTimeMillis()/1000;
                    } else {
                        unixSeconds = new Long(field.getText());
                    }
                    Date date2 = new Date(unixSeconds*1000L);
                    myCalendar.setTime(date2);

                    final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear,
                                              int dayOfMonth) {
                            // TODO Auto-generated method stub
                            myCalendar.set(Calendar.YEAR, year);
                            myCalendar.set(Calendar.MONTH, monthOfYear);
                            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            //String myFormat = "dd.MM.yyyy"; //In which you need put here
                           // SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                           // et.setText(sdf.format(myCalendar.getTime()));
                        }

                    };

                    final DatePickerDialog dial = new DatePickerDialog(ViewPagerSearchActivity.this, date, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH));
                    dial.setTitle("Редактирование");
                    dial.setButton(DatePickerDialog.BUTTON_POSITIVE, "Применить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //adapters.get(viewPager.getCurrentItem()).getItem(info.position).setText();
                            int day = dial.getDatePicker().getDayOfMonth();
                            int month = dial.getDatePicker().getMonth();
                            int year = dial.getDatePicker().getYear();
                            String time = Long.toString((new Date(year-1900,month,day)).getTime()/1000);

                            try {
                                ApplicationParameters.tempResults.getJSONObject(viewPager.getCurrentItem()).put(ApplicationParameters.getTableFieldKeyByName(field.getName(), singleFields), time);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Button bt = (Button)pages.get(viewPager.getCurrentItem()).findViewById(R.id.saveChangesButton);
                            bt.setVisibility(View.VISIBLE);
                            adapters.get(viewPager.getCurrentItem()).getItem(info.position).setText(time);
                            ArrayList<TripleTableField> obs = adapters.get(viewPager.getCurrentItem()).objects;
                            TripleTableField changed = obs.get(info.position);
                            obs.set(info.position, new TripleTableField(changed.getName(), time,changed.getType()));
                            adapters.get(viewPager.getCurrentItem()).isChanged(true);
                            adapters.get(viewPager.getCurrentItem()).notifyDataSetChanged();
                        }
                    });
                    dial.show();

                } else {
                    input.setText(field.getText());
                    final TripleTableField[] book = new TripleTableField[1];
                    if (field.getType().equals("dict")) {
                        input.setThreshold(1);
                        input.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                                book[0] = (TripleTableField) adapterView.getItemAtPosition(position);
                                input.setText(book[0].getText());
                            }
                        });

                        DictionaryAutoCompleteAdapter autoCompleteAdapter = new DictionaryAutoCompleteAdapter(getApplicationContext(), ApplicationParameters.getTableFieldByKey(ApplicationParameters.getTableFieldKeyByName(field.getName(), ApplicationParameters.tableFields), ApplicationParameters.dictionaries));
                        //autoCompleteAdapter.setActivity(NewSearchActivity.this);
                        input.setAdapter(autoCompleteAdapter);
                    }
                    new AlertDialog.Builder(ViewPagerSearchActivity.this)
                            .setTitle("Редактирование")
                            .setMessage("Введите новое значение")
                            .setView(input)
                            .setPositiveButton("Применить", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    try {
                                        if (field.getType().equals("dict"))
                                            addChangedDictField(new TripleTableField(ApplicationParameters.getTableFieldByKey(ApplicationParameters.getTableFieldKeyByName(field.getName(), ApplicationParameters.tableFields), ApplicationParameters.dictionaries).getName(), book[0].getName()));
                                        Editable value = input.getText();
                                        try {
                                            ApplicationParameters.tempResults.getJSONObject(viewPager.getCurrentItem()).put(ApplicationParameters.getTableFieldKeyByName(field.getName(), singleFields), value.toString());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        Button bt = (Button) pages.get(viewPager.getCurrentItem()).findViewById(R.id.saveChangesButton);
                                        bt.setVisibility(View.VISIBLE);
                                        ArrayList<TripleTableField> obs = adapters.get(viewPager.getCurrentItem()).objects;
                                        TripleTableField changed = obs.get(info.position);
                                        adapters.get(viewPager.getCurrentItem()).getItem(info.position).setText(value.toString());
                                        obs.set(info.position, new TripleTableField(changed.getName(), value.toString(), changed.getType()));
                                        adapters.get(viewPager.getCurrentItem()).isChanged(true);
                                        adapters.get(viewPager.getCurrentItem()).notifyDataSetChanged();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            }).setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Do nothing.
                        }
                    }).show();
                }

                break;
            case R.id.cancel: closeContextMenu(); break;
            default: break;
        }
        return true;
    }
}
