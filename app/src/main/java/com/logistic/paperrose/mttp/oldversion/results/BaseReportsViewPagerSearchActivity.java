package com.logistic.paperrose.mttp.oldversion.results;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Toast;

import com.logistic.paperrose.mttp.oldversion.BaseLogisticActivity;
import com.logistic.paperrose.mttp.oldversion.LoginActivity;
import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.newsearch.BaseSearchPageAdapter;
import com.logistic.paperrose.mttp.oldversion.newsearch.DictionaryAutoCompleteAdapter;
import com.logistic.paperrose.mttp.oldversion.search.DelayedFilteringField;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;
import com.logistic.paperrose.mttp.oldversion.utils.ExtendedEditText;
import com.logistic.paperrose.mttp.oldversion.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class BaseReportsViewPagerSearchActivity extends BaseLogisticActivity {

    List<View> pages = new ArrayList<View>();
    ViewPager viewPager;
    LayoutInflater inflater;
    public JSONArray jsonResults;
    public BaseReportsSearchResults parentActivity = null;
    public int lastNum;
    public int currentPageNum;
    BaseReportsSearchViewPagerAdapter pagerAdapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLayoutId(R.layout.base_activity_view_pager_search);
        super.onCreate(savedInstanceState);
        inflater = LayoutInflater.from(BaseReportsViewPagerSearchActivity.this);
        viewPager = (ViewPager)findViewById(R.id.viewPager);
        try {
            jsonResults = ApplicationParameters.tempResults;
            if (jsonResults == null) {
                jsonResults = new JSONArray();
            }
            for (int i = 0; i < jsonResults.length(); i++) {
                createPage(jsonResults.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (ApplicationParameters.chosenDeclarationReportsSingleFields.size() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(BaseReportsViewPagerSearchActivity.this);
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

        pagerAdapter = new BaseReportsSearchViewPagerAdapter(pages);
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
        try {
            parentActivity = (BaseReportsSearchResults) ApplicationParameters.activity;
        } catch (Exception e) {

        }
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
    }


    public void refresh(JSONArray results) {
        pages.clear();
        jsonResults = results;
        try {
            if (jsonResults == null) {
                jsonResults = new JSONArray();
            }
            for (int i = 0; i < jsonResults.length(); i++) {
                createPage(jsonResults.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        BaseReportsSearchViewPagerAdapter pagerAdapter = new BaseReportsSearchViewPagerAdapter(pages);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(lastNum);
        pagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (adapters.get(currentPageNum).isChanged()) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            // заголовок
            adb.setTitle("Предупреждение");
            // сообщение
            adb.setMessage("Сохранить изменения в записи?");
            // иконка
            adb.setIcon(android.R.drawable.ic_dialog_info);
            // кнопка положительного ответа
            adb.setPositiveButton("Все равно продолжить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                backPressed();
                }
            });
            adb.setNeutralButton("Вернуться и сохранить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            adb.show();
        } else {
            backPressed();
        }
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

                jsonObject = checkServerCredentials2(args[0], cc, Integer.toString(ApplicationParameters.offset*25));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            showProgress(false);
            if (res == null) {
                Toast.makeText(BaseReportsViewPagerSearchActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
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
                        Intent intent = new Intent(BaseReportsViewPagerSearchActivity.this, LoginActivity.class);
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
                    JSONArray arr2 = new JSONArray();
                    ApplicationParameters.lastResults = null;
                    ApplicationParameters.tempResults = new JSONArray();
                    System.gc();
                    ApplicationParameters.lastResults = new JSONArray(res.getString("result"));

                    for (int i = 0; i < ApplicationParameters.lastResults.length(); i++) {
                        ApplicationParameters.tempResults.put(ApplicationParameters.lastResults.get(i));
                    }

                    if (ApplicationParameters.chosenDeclarationReportsSingleFields.size() == 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(BaseReportsViewPagerSearchActivity.this);
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //return true;
            }
        }
    }

    public void backPressed() {
        super.onBackPressed();
    }

    public void createPage(JSONObject obj) throws JSONException {
        View page = inflater.inflate(R.layout.base_page_view, null);
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
                (new CheckToken()).execute();
            }
        });
        Iterator<String> keys = obj.keys();
        ArrayList<TripleTableField> objS = new ArrayList<TripleTableField>();
        ArrayList<String> copy = new ArrayList<String>();
        while (keys.hasNext())
            copy.add(keys.next());
        for (int i = 0; i < ApplicationParameters.chosenDeclarationReportsSingleFields.size(); i++) {
            for (int k = 0; k < copy.size(); k++) {
                String key = copy.get(k);
                if (ApplicationParameters.chosenDeclarationReportsSingleFields.get(i).getName().equals(key)  && !obj.getString(key).isEmpty()) {
                    objS.add(
                            new TripleTableField(
                                    ApplicationParameters.getTableFieldNameByKey(key, ApplicationParameters.declarationFields),
                                    obj.getString(key),
                                    ApplicationParameters.getFieldType(key, ApplicationParameters.declarationFields)
                            )
                    );
                }
            }
        }
        final BaseSearchPageAdapter adapter = new BaseSearchPageAdapter(BaseReportsViewPagerSearchActivity.this, R.layout.search_field_row, objS);
        adapter.pageID = Integer.parseInt(obj.getString("DEC_ID"));
        Button bt2 = (Button)page.findViewById(R.id.docsButton);
        bt2.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View view) {
                                       Intent intent = new Intent(BaseReportsViewPagerSearchActivity.this, DownloadDocuments.class);
                                       intent.putExtra("number", Integer.toString(adapter.pageID));
                                       startActivity(intent);
                                       //finish();
                                   }
                               }

        );
        Button bt3 = (Button)page.findViewById(R.id.edButton);
        bt3.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View view) {
                                       Intent intent = new Intent(BaseReportsViewPagerSearchActivity.this, EDDocs.class);
                                       intent.putExtra("number", Integer.toString(adapter.pageID));
                                       startActivity(intent);
                                       //finish();
                                   }
                               }

        );
        try {
            adapter.handMode = obj.getString("DEC_IS_HAND").equals("1");

        } catch (Exception e) {
            adapter.handMode = true;
        }
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
        JSONObject obj = parser.getJSONFromUrl("http://"+ApplicationParameters.MAIN_DOMAIN +"/declarations/edit", new HashMap<String, String>() {{
            put("access_token", access_token);
            put("hs", secure_token);
            put("edit_fields", getJSONParameters());
        }}, getApplicationContext());
        return obj;
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
                Toast.makeText(BaseReportsViewPagerSearchActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
            } else {
                changedDictFields.clear();
                if (res.has("error")) {
                    Intent intent = new Intent(BaseReportsViewPagerSearchActivity.this, LoginActivity.class);
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

    @Override
    public void onResume() {
        super.onResume();
        if (ApplicationParameters.changed) {
            refreshRequest();
            ApplicationParameters.changed = false;
        }
    }

    public String getJSONParameters() {
        JSONArray arr = new JSONArray();
        for (int i = 0; i < ApplicationParameters.chosenDeclarationReportsSingleFields.size(); i++) {
            TripleTableField cc = ApplicationParameters.chosenDeclarationReportsSingleFields.get(i);
            JSONObject obj = new JSONObject();
            try {
                String key = ApplicationParameters.getTableFieldKeyByName(adapters.get(currentPageNum).getItem(i).getName(), ApplicationParameters.declarationFields);
                if (!(key.contains("DEC_SUM_VALUE1") || key.contains("DEC_SUM_VALUE2") || key.contains("DEC_IS_TS") || key.contains("DEC_LIST_INSPECTOR_CUS_VET_NAME") || key.contains("DEC_NOTE"))) continue;
                if (!adapters.get(currentPageNum).handMode && key.contains("DEC_SUM_VALUE1")) {
                    continue;
                }
                if (adapters.get(currentPageNum).getItem(i).getType().equals("dict") ) {

                    String val = getChangedDictFieldID(key);
                    if (val != null) {
                        obj.put("field_name", ApplicationParameters.getTableFieldByKey(key, ApplicationParameters.declarationDictionaries).additional);
                        obj.put("field_type", adapters.get(currentPageNum).getItem(i).getType());
                        obj.put("field_value", val);
                        arr.put(obj);
                    }
                } else {


                    obj.put("field_name", key);
                    obj.put("field_type", adapters.get(currentPageNum).getItem(i).getType());
                    obj.put("field_value", adapters.get(currentPageNum).getItem(i).getText());
                    arr.put(obj);
                }

            } catch (Exception e) {
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
        View page = inflater.inflate(R.layout.base_page_view, null);
        ListView lv = (ListView) page.findViewById(R.id.searchResult);
        final Button bt = (Button)page.findViewById(R.id.saveChangesButton);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bt.setVisibility(View.GONE);
            }
        });
        //Button bt2 = (Button)page.findViewById(R.id.docsButton);
        //bt2.setVisibility(View.GONE);
        Iterator<String> keys = obj.keys();
        ArrayList<TripleTableField> objS = new ArrayList<TripleTableField>();
        ArrayList<String> copy = new ArrayList<String>();
        while (keys.hasNext())
            copy.add(keys.next());
        for (int i = 0; i < ApplicationParameters.declarationFields.size(); i++) {
            for (int k = 0; k < copy.size(); k++) {
                String key = copy.get(k);
                if (ApplicationParameters.declarationFields.get(i).getName().equals(key)  && !obj.getString(key).isEmpty()) {
                    objS.add(
                            new TripleTableField(
                                    ApplicationParameters.getTableFieldNameByKey(key, ApplicationParameters.declarationFields),
                                    obj.getString(key),
                                    ApplicationParameters.getFieldType(key, ApplicationParameters.declarationFields)
                            )
                    );
                }
            }
        }
        BaseSearchPageAdapter adapter = new BaseSearchPageAdapter(BaseReportsViewPagerSearchActivity.this, R.layout.search_field_row, objS);
        lv.setAdapter(adapter);
        currentPageNum = viewPager.getCurrentItem();
        registerForContextMenu(lv);
        adapters.set(index, adapter);
        pages.set(index, page);
        pagerAdapter = new BaseReportsSearchViewPagerAdapter(pages);
        pagerAdapter.notifyDataSetChanged();
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(currentPageNum);
        //viewPager.se

    }


    ArrayList<BaseSearchPageAdapter> adapters = new ArrayList<BaseSearchPageAdapter>();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base_reports_view_pager_search, menu);
        return true;
    }


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
        final AdapterView.AdapterContextMenuInfo info;
        try {
            int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
            final TripleTableField field = adapters.get(viewPager.getCurrentItem()).getItem(position);
            String key = ApplicationParameters.getTableFieldKeyByName(field.getName(), ApplicationParameters.declarationFields);
            if (!(key.contains("DEC_SUM_VALUE1") || key.contains("DEC_SUM_VALUE2") || key.contains("DEC_IS_TS") || key.contains("DEC_LIST_INSPECTOR_CUS_VET_NAME") || key.contains("DEC_NOTE"))) {
                return;
            }
            if (!adapters.get(viewPager.getCurrentItem()).handMode && key.contains("DEC_SUM_VALUE1")) {
                return;
            }
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
        } catch (ClassCastException e) {
            return;
        }

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
                String key = ApplicationParameters.getTableFieldKeyByName(field.getName(), ApplicationParameters.declarationFields);
                if (key.contains("DEC_SUM_VALUE1") || key.contains("DEC_SUM_VALUE2")) {
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                }

                if (!(key.contains("DEC_SUM_VALUE1") || key.contains("DEC_SUM_VALUE2") || key.contains("DEC_IS_TS") || key.contains("DEC_LIST_INSPECTOR_CUS_VET_NAME") || key.contains("DEC_NOTE"))) {
                    break;
                }
                if (!adapters.get(viewPager.getCurrentItem()).handMode && key.contains("DEC_SUM_VALUE1")) {
                    break;
                }
                if (field.getType().equals("date")) {

                    final Calendar myCalendar = Calendar.getInstance();
                    long unixSeconds = new Long(field.getText());
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

                    final DatePickerDialog dial = new DatePickerDialog(BaseReportsViewPagerSearchActivity.this, date, myCalendar
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
                                ApplicationParameters.tempResults.getJSONObject(viewPager.getCurrentItem()).put(ApplicationParameters.getTableFieldKeyByName(field.getName(), ApplicationParameters.declarationFields), time);

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

                        DictionaryAutoCompleteAdapter autoCompleteAdapter = new DictionaryAutoCompleteAdapter(getApplicationContext(), ApplicationParameters.getTableFieldByKey(ApplicationParameters.getTableFieldKeyByName(field.getName(), ApplicationParameters.declarationFields), ApplicationParameters.dictionaries));
                        //autoCompleteAdapter.setActivity(NewSearchActivity.this);
                        input.setAdapter(autoCompleteAdapter);
                    }

                    new AlertDialog.Builder(BaseReportsViewPagerSearchActivity.this)
                            .setTitle("Редактирование")
                            .setMessage("Введите новое значение")
                            .setView(input)
                            .setPositiveButton("Применить", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    try {
                                        if (field.getType().equals("dict"))
                                            addChangedDictField(new TripleTableField(ApplicationParameters.getTableFieldByKey(ApplicationParameters.getTableFieldKeyByName(field.getName(), ApplicationParameters.declarationFields), ApplicationParameters.declarationDictionaries).getName(), book[0].getName()));
                                        Editable value = input.getText();
                                        try {
                                            ApplicationParameters.tempResults.getJSONObject(viewPager.getCurrentItem()).put(ApplicationParameters.getTableFieldKeyByName(field.getName(), ApplicationParameters.declarationFields), value.toString());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        Button bt = (Button) pages.get(viewPager.getCurrentItem()).findViewById(R.id.saveChangesButton);
                                        bt.setVisibility(View.VISIBLE);
                                        ArrayList<TripleTableField> obs = adapters.get(viewPager.getCurrentItem()).objects;
                                        TripleTableField changed = obs.get(info.position);
                                        obs.set(info.position, new TripleTableField(changed.getName(), value.toString(), changed.getType()));
                                        adapters.get(viewPager.getCurrentItem()).isChanged(true);
                                        adapters.get(viewPager.getCurrentItem()).notifyDataSetChanged();
                                    } catch (Exception e) {

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
