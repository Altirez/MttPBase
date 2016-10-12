package com.logistic.paperrose.mttp.oldversion.newsearch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.Menu;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.logistic.paperrose.mttp.oldversion.LoginActivity;
import com.logistic.paperrose.mttp.oldversion.MainActivity;
import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.clients.SettingsType;
import com.logistic.paperrose.mttp.oldversion.dragndrop.DragListener;
import com.logistic.paperrose.mttp.oldversion.dragndrop.DragNDropListView;
import com.logistic.paperrose.mttp.oldversion.dragndrop.DropListener;
import com.logistic.paperrose.mttp.oldversion.dragndrop.RemoveListener;
import com.logistic.paperrose.mttp.oldversion.results.BaseSearchResults;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.settings.AlertFieldsAdapter;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;
import com.logistic.paperrose.mttp.oldversion.settings.Bookmark;
import com.logistic.paperrose.mttp.oldversion.settings.ChosenFieldsAdapter;
import com.logistic.paperrose.mttp.oldversion.settings.ChosenFieldsAdapterVer2;
import com.logistic.paperrose.mttp.oldversion.utils.ConnectionActivity;
import com.logistic.paperrose.mttp.oldversion.utils.ExtendedEditText;
import com.logistic.paperrose.mttp.oldversion.utils.JSONParser;
import com.logistic.paperrose.mttp.oldversion.utils.JSONStreamReader;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class BaseNewSearchActivity extends ConnectionActivity {


    ChosenFieldsAdapter adapter;
    DecFiltersAdapter adapterChosen;
    int bookmarkID = -1;


    private ArrayList<TripleTableField> loadSettings() {
        SharedPreferences historyPrefs = getSharedPreferences("UserPreference", 0);
        Gson gson = new Gson();
        String jsonH = historyPrefs.getString("declarations", "");
        //String jsonS = historyPrefs.getString("filters", "");
        Type type = new TypeToken<ArrayList<TripleTableField>>() {
        }.getType();
        if (jsonH != "") {
            ApplicationParameters.declarationFields = gson.fromJson(jsonH, type);
        }
        //if (jsonS == "") {
        ApplicationParameters.chosenDeclarationSearchFields = new ArrayList<TripleTableField>();
        try {
            if (ApplicationParameters.decLastSearchString != null) {
                JSONArray filters = ApplicationParameters.decLastSearchString.getJSONArray("filters");
                for (int i = 0; i < filters.length(); i++) {
                    TripleTableField tr = ApplicationParameters.getTableFieldByKey(filters.getJSONObject(i).getString("field_name"), ApplicationParameters.declarationFields);
                    if (tr != null)
                        ApplicationParameters.chosenDeclarationSearchFields.add(tr);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new ArrayList<TripleTableField>();
    }

    @Override
    protected void onDestroy() {
        // ;

        saveChanges(getSharedPreferences("UserPreference", 0));
        super.onDestroy();
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
            showProgress(false);
            if (res == null) {
                Toast.makeText(BaseNewSearchActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
            } else

            if (res.has("error")) {
                try {
                    if (res.getString("error").equals("Results count is too big")) {
                        showDialog(2);
                        return;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(BaseNewSearchActivity.this, LoginActivity.class);
                intent.putExtra("login", true);
                startActivity(intent);
                finish();
            } else {
                try {

                    saveCredentials(res.getString("access_token"), "access_token");
                    JSONArray arr = new JSONArray(res.getString("result"));
                    ApplicationParameters.currentLength = Integer.parseInt(res.getString("count"));
                    try {
                        ApplicationParameters.setTrafficDocuments(new JSONArray(res.getString("documents")));
                    } catch (Exception e) {

                    }
                    len = arr.length();
                    result = res;
                    showDialog(1);
                    //ApplicationParameters.testTableFields();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //return true;
            }
        }
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void refreshRequest() {
        final EditText input = new EditText(BaseNewSearchActivity.this);
        if (bookmarkID < 0) {

            final boolean[] add_client_error = {false};

            AlertDialog.Builder builder = new AlertDialog.Builder(BaseNewSearchActivity.this)
                    .setTitle("Сохранить закладку")
                    .setMessage("Введите имя для закладки")
                    .setView(input)
                    .setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            final Editable value = input.getText();
                            if (value.toString().isEmpty()) {
                                add_client_error[0] = true;
                                Toast.makeText(getApplicationContext(), "Необходимо задать имя закладки", Toast.LENGTH_LONG).show();
                                return;
                            }
                            final String jsonParameters = getJSONParametersString();
                            if (jsonParameters.isEmpty()) {
                                Toast.makeText(getApplicationContext(), "Необходимо задать фильтры", Toast.LENGTH_LONG).show();
                                return;
                            }

                            new AsyncTask<Pair<String, HashMap<String, String>>, Void, JSONObject>() {
                                JSONObject checkServerCredentials2() throws JSONException {
                                    final String access_token = getPref("access_token");
                                    final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
                                    JSONParser parser = new JSONParser();
                                    JSONObject obj = parser.getJSONFromUrl("http://" + ApplicationParameters.MAIN_DOMAIN + "/add_bookmark", new HashMap<String, String>() {{
                                        put("access_token", access_token);
                                        put("hs", secure_token);
                                        put("name", value.toString());
                                        put("description", jsonParameters);
                                        ArrayList<TripleTableField> p1 = ApplicationParameters.chosenDeclarationSearchFields.size() > 0 ? ApplicationParameters.chosenDeclarationSearchFields : ApplicationParameters.declarationFields;
                                        ArrayList<TripleTableField> p2 = ApplicationParameters.chosenDeclarationSingleFields.size() > 0 ? ApplicationParameters.chosenDeclarationSingleFields : ApplicationParameters.declarationFields;
                                        put("order", ApplicationParameters.getOrder(p1, p2));
                                        put("type", "2");
                                    }}, getApplicationContext());
                                    return obj;
                                }

                                @Override
                                protected JSONObject doInBackground(Pair<String, HashMap<String, String>>... args) {
                                    JSONObject jsonObject = null;
                                    if (ApplicationParameters.GCM_KEY.isEmpty()) return new JSONObject() {{
                                        try {
                                            put("error", "err");
                                            put("status_code", 401);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }};
                                    try {
                                        jsonObject = checkServerCredentials2();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    return jsonObject;
                                }

                                @Override
                                protected void onPostExecute(JSONObject res) {
                                    if (res == null) {
                                        //      Toast.makeText(NewSearchActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
                                    } else if (res.has("error")) {
                                        Intent intent = new Intent(BaseNewSearchActivity.this, LoginActivity.class);
                                        intent.putExtra("login", true);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        try {
                                            saveCredentials(res.getString("access_token"), "access_token");
                                            Bookmark bm = new Bookmark(Long.parseLong(res.getJSONArray("res").getJSONObject(0).getString("ID")), value.toString(), jsonParameters, res.getString("fields_order"), 2);
                                            ApplicationParameters.bookmarks.add(bm);
                                            try {
                                                ApplicationParameters.activity.saveBookmarks();
                                                ApplicationParameters.activity.setDrawerMenu();
                                            } catch (NullPointerException e) {

                                                BaseNewSearchActivity.this.saveBookmarks();
                                                BaseNewSearchActivity.this.setDrawerMenu();
                                            }
                                            Toast.makeText(getApplicationContext(), "Закладка '" + value.toString() + "' создана", Toast.LENGTH_LONG).show();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }.execute();

                        }
                    }).setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            add_client_error[0] = false;
                        }
                    });
            final AlertDialog alertClient = builder.create();
            alertClient.show();
            alertClient.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    //If the error flag was set to true then show the dialog again
                    if (add_client_error[0] == true) {
                        alertClient.show();
                    } else {
                        return;
                    }

                }
            });
        } else {
            input.setText(ApplicationParameters.getBookmarkNameByID(bookmarkID));
            final boolean[] add_client_error = {false};
            AlertDialog.Builder builder = new AlertDialog.Builder(BaseNewSearchActivity.this)
                    .setTitle("Сохранить закладку")
                    .setMessage("Вы уверены, что хотите сохранить новые фильтры?")
                    .setView(input)
                    .setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            final String jsonParameters = getJSONParametersString();
                            final Editable value = input.getText();
                            if (value.toString().isEmpty()) {
                                add_client_error[0] = true;
                                Toast.makeText(getApplicationContext(), "Необходимо задать имя закладки", Toast.LENGTH_LONG).show();
                                return;
                            }
                            new AsyncTask<Pair<String, HashMap<String, String>>, Void, JSONObject>() {
                                JSONObject checkServerCredentials2() throws JSONException {
                                    final String access_token = getPref("access_token");
                                    final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
                                    JSONParser parser = new JSONParser();
                                    JSONObject obj = parser.getJSONFromUrl("http://" + ApplicationParameters.MAIN_DOMAIN + "/edit_bookmark", new HashMap<String, String>() {{
                                        put("access_token", access_token);
                                        put("hs", secure_token);
                                        put("name", value.toString());
                                        put("description", jsonParameters);
                                        put("bookmark_id", Integer.toString(bookmarkID));
                                    }}, getApplicationContext());
                                    return obj;
                                }

                                @Override
                                protected JSONObject doInBackground(Pair<String, HashMap<String, String>>... args) {
                                    JSONObject jsonObject = null;
                                    try {
                                        jsonObject = checkServerCredentials2();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    return jsonObject;
                                }

                                @Override
                                protected void onPostExecute(JSONObject res) {
                                    if (res == null) {
                                        //     Toast.makeText(NewSearchActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
                                    } else if (res.has("error")) {
                                        Intent intent = new Intent(BaseNewSearchActivity.this, LoginActivity.class);
                                        intent.putExtra("login", true);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        try {
                                            saveCredentials(res.getString("access_token"), "access_token");
                                            ApplicationParameters.setBookmarkDescByID(bookmarkID, jsonParameters);
                                            ApplicationParameters.activity.saveBookmarks();
                                            ApplicationParameters.activity.setDrawerMenu();
                                            Toast.makeText(getApplicationContext(), "Закладка '" + value.toString() + "' изменена", Toast.LENGTH_LONG).show();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (NullPointerException e) {

                                            BaseNewSearchActivity.this.saveBookmarks();
                                            BaseNewSearchActivity.this.setDrawerMenu();
                                        }
                                    }
                                }
                            }.execute();

                        }
                    }).setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Do nothing.
                        }
                    });
            final AlertDialog alertClient = builder.create();
            alertClient.show();
            alertClient.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    //If the error flag was set to true then show the dialog again
                    if (add_client_error[0]) {
                        alertClient.show();
                    } else {
                        return;
                    }

                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_search, menu);
        return true;
    }


    private int len;



    protected Dialog onCreateDialog(int id) {
        if (id == 1) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            // заголовок
            adb.setTitle("Предупреждение");
            // сообщение
            adb.setMessage("По данным критериям поиска найдено " + Integer.toString(len) + " " + unreadCountText(len) + ". Нажмите \"Отмена\", если хотите уточнить поиск.");
            // иконка
            adb.setIcon(android.R.drawable.ic_dialog_info);
            // кнопка положительного ответа
            adb.setPositiveButton("Продолжить", myClickListener);
            // кнопка отрицательного ответа
            adb.setNeutralButton("Отмена", myClickListener);
            // кнопка нейтрального ответа
            // создаем диалог
            return adb.create();
        }
        if (id == 2) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            // заголовок
            adb.setTitle("Предупреждение");
            // сообщение
            adb.setMessage("Найдено более 250 результатов, пожалуйста уточните критерии поиска");
            // иконка
            adb.setIcon(android.R.drawable.ic_dialog_info);

            adb.setNeutralButton("Ок", myClickListener);
            return adb.create();
        }
        if (id == 3) {

        }
        return super.onCreateDialog(id);
    }




    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                // положительная кнопка
                case Dialog.BUTTON_POSITIVE:

                    showProgress(true);
                    Intent intent = new Intent(BaseNewSearchActivity.this, BaseSearchResults.class);
                    try {
                        ApplicationParameters.lastResults = new JSONArray();
                        ApplicationParameters.tempResults = new JSONArray();
                        String res = result.getString("result");
                        JSONStreamReader.readJsonStreamToTr(res, ApplicationParameters.lastResults);
                        JSONStreamReader.readJsonStreamToTr(res, ApplicationParameters.tempResults);
                       // intent.putExtra("json_results", arr.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    startActivity(intent);
                    finish();
                    break;
                // нейтральная кнопка
                case Dialog.BUTTON_NEUTRAL:
                    break;
            }
        }
    };

    public String getPref(String key) {
        return getSharedPreferences("UserPreference", 0).getString(key, "123");
    }


    public JSONObject checkServerCredentials() throws JSONException {
        final String access_token = getPref("access_token");
        final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
        JSONParser parser = new JSONParser();
        JSONObject obj = parser.getJSONFromUrl("http://"+ApplicationParameters.MAIN_DOMAIN +"/declarations", new HashMap<String, String>() {{
            put("access_token", access_token);
            put("type", "search");
            put("hs", secure_token);
            put("filter_object", getJSONParameters());
        }}, getApplicationContext());
        return obj;
    }

    public String getFieldsString() {
        String cs = "";
        if (ApplicationParameters.chosenDeclarationSearchFields.size() > 0)
            cs = ApplicationParameters.chosenDeclarationSearchFields.get(0).getName();
        for (int i = 1; i < ApplicationParameters.chosenDeclarationSearchFields.size(); i++) {
            cs += "," + ApplicationParameters.chosenDeclarationSearchFields.get(i).getName();
        }
        return cs;
    }


    public void saveChanges(SharedPreferences prefs) {
        SharedPreferences historyPrefs = prefs;// = getSharedPreferences("UserPreference", 0);
        SharedPreferences.Editor prefsEditor = historyPrefs.edit();
        Gson gson = new Gson();
        String json2 = gson.toJson(ApplicationParameters.chosenDeclarationSearchFields);
        // String json1 = gson.toJson(ApplicationParameters.tableFields);
        // prefsEditor.putString("all", json1);
        prefsEditor.putString("dec_filters", json2);
        prefsEditor.commit();
    }

    private DropListener mDropListener =
            new DropListener() {
                public void onDrop(int from, int to) {
                    if (adapterChosen instanceof ChosenFieldsAdapterVer2) {
                        adapterChosen.onDrop(from, to);
                        lvChosen.invalidateViews();
                    }
                }
            };

    private RemoveListener mRemoveListener =
            new RemoveListener() {
                public void onRemove(int which) {
                    if (adapterChosen instanceof ChosenFieldsAdapterVer2) {
                        adapterChosen.onRemove(which);
                        lvChosen.invalidateViews();
                    }
                }
            };

    private DragListener mDragListener =
            new DragListener() {

                int backgroundColor = 0x44FF1C00;
                int defaultBackgroundColor;

                public void onDrag(int x, int y, ListView listView) {
                    // TODO Auto-generated method stub
                }

                public void onStartDrag(View itemView) {
                    itemView.setVisibility(View.GONE);
                    defaultBackgroundColor = itemView.getDrawingCacheBackgroundColor();
                    itemView.setBackgroundColor(backgroundColor);
                    ImageView iv = (ImageView) itemView.findViewById(R.id.upItem);
                    if (iv != null) iv.setVisibility(View.VISIBLE);
                }

                public void onStopDrag(View itemView) {
                    itemView.setVisibility(View.VISIBLE);
                    itemView.setBackgroundColor(defaultBackgroundColor);
                    ImageView iv = (ImageView) itemView.findViewById(R.id.upItem);
                    if (iv != null) iv.setVisibility(View.VISIBLE);
                }

            };

    DragNDropListView lvChosen;

    @Override
    public String getJSONParameters() {
//        String mainF = ((DelayedFilteringField) findViewById(R.id.simpleSearchField)).getText().toString();
        JSONArray arr = new JSONArray();
        Iterator<String> keys;
        keys = adapterChosen.fieldFilters.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject obj = new JSONObject();
            try {
                String type = ApplicationParameters.getFieldType(key, ApplicationParameters.declarationFields);
                obj.put("field_name", key);
                obj.put("field_type", type);
                obj.put("field_value", adapterChosen.fieldFilters.get(key));
                if (type.equals("date") && adapterChosen.fieldFilters.get(key+"___2") != null) {
                    obj.put("field_value2", adapterChosen.fieldFilters.get(key+"___2"));
                }
                if (type.equals("dict")) {

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            arr.put(obj);
        }
        JSONObject res = new JSONObject();
        try {
//            res.put("main_filter", mainF);
            res.put("filters", arr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApplicationParameters.decLastSearchString = res;
        return res.toString();
    }

    public String getJSONParametersString() {
        JSONArray arr = new JSONArray();
        Iterator<String> keys;
        keys = adapterChosen.fieldFilters.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.contains("___2")) continue;
            JSONObject obj = new JSONObject();
            try {
                String type = ApplicationParameters.getFieldType(key, ApplicationParameters.declarationFields);
                if (type == "dict") {
                    TripleTableField tff = ApplicationParameters.getTableFieldByKey(key, ApplicationParameters.declarationDictionaries);
                    obj.put("field_name", tff.getType() + "." + tff.getText());
                }//type.text
                else
                    obj.put("field_name", key);
                obj.put("field_type", type);
                if (type == "dict")
                obj.put("field_value", adapterChosen.fieldFilters.get(key));
                if (type.equals("date") && adapterChosen.fieldFilters.get(key+"___2") != null) {
                    obj.put("field_value2", adapterChosen.fieldFilters.get(key+"___2"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            arr.put(obj);
        }
        JSONObject res = new JSONObject();
        try {
            res.put("filters", arr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return res.toString();
    }


    boolean isOpened = false;

    public void setListenerToRootView() {
        final View activityRootView = getWindow().getDecorView().findViewById(android.R.id.content);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
               /* if (heightDiff > 112) { // 99% of the time the height diff will be due to a keyboard.

                    if (isOpened == false) {
                        findViewById(R.id.buttons).setVisibility(View.GONE);
                        findViewById(R.id.real_buttons).setVisibility(View.GONE);
                        findViewById(R.id.counter).setVisibility(View.GONE);
                        findViewById(R.id.searchButton).setVisibility(View.GONE);
                    }
                    isOpened = true;
                } else if (isOpened == true) {
                 */   SharedPreferences unread = getSharedPreferences("UserPreference", 0);
                    int unread_count = unread.getInt("unread", 0);
                    findViewById(R.id.counter).setVisibility(unread_count == 0 ? View.GONE : View.VISIBLE);
                    findViewById(R.id.buttons).setVisibility(View.VISIBLE);
                    findViewById(R.id.real_buttons).setVisibility(View.VISIBLE);
                    findViewById(R.id.searchButton).setVisibility(View.VISIBLE);
               /*     isOpened = false;
                }*/
            }
        });
    }

    AlertFieldsAdapter alertAdapter;
    ListView lvMain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLayoutId(R.layout.base_activity_new_search);
        super.onCreate(savedInstanceState);

        // this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        //4this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        bookmarkID = getIntent().getIntExtra("bookmarkID", -1);
        final ArrayList<TripleTableField> used = new ArrayList<TripleTableField>();
        for (int i = 0; i < ApplicationParameters.chosenDeclarationSearchFields.size(); i++) {
            used.add(ApplicationParameters.chosenDeclarationSearchFields.get(i));
        }
        ExtendedEditText et = (ExtendedEditText) findViewById(R.id.search);
        et.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString());
                adapter.filterS = s;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        Button button = (Button) findViewById(R.id.searchButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress(true);
                saveChanges(getSharedPreferences("UserPreference", 0));

                (new CheckToken()).execute();
            }
        });

        loadSettings();
        lvMain = (ListView) findViewById(R.id.pushFieldsList);
        lvChosen = (DragNDropListView) findViewById(R.id.chosenPushFieldsList);
        adapter = new ChosenFieldsAdapter(this, R.layout.activity_new_search, ApplicationParameters.declarationFields, lvMain, ApplicationParameters.declarationFields, ApplicationParameters.chosenDeclarationSearchFields, R.layout.text_edit_list_layout);
        adapter.sType = SettingsType.DEC_TABLE;
        adapter.setUsedObjects(used);
        adapterChosen = new DecFiltersAdapter(this, R.layout.activity_new_search, ApplicationParameters.chosenDeclarationSearchFields, lvChosen);
        adapterChosen.setChosen(true);
        adapterChosen.firstListAdapter = adapter;
        adapter.secondListAdapter = adapterChosen;

        lvMain.setAdapter(adapter);

        lvChosen.setAdapter(adapterChosen);






        Button alertButton = (Button)findViewById(R.id.addFields);
        alertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LinearLayout view2 = (LinearLayout) getLayoutInflater()
                        .inflate(R.layout.custom_dialog, null);

                lvMain = (ListView)view2.findViewById(R.id.alertFieldsList);

                alertAdapter = new AlertFieldsAdapter(BaseNewSearchActivity.this, R.layout.activity_push_settings_ver2, ApplicationParameters.declarationFields, lvMain, ApplicationParameters.declarationFields, ApplicationParameters.chosenDeclarationSearchFields);
                alertAdapter.secondListAdapter = adapterChosen;
                alertAdapter.type = 1;
                lvMain.setAdapter(alertAdapter);

                ExtendedEditText et = (ExtendedEditText) view2.findViewById(R.id.search);
                et.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        alertAdapter.getFilter().filter(s.toString());
                        alertAdapter.filterS = s;
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
                AlertDialog.Builder builder = new AlertDialog.Builder(BaseNewSearchActivity.this);
                builder.setTitle("Добавление полей");
                builder.setMessage("Выберите поля");
                builder.setView(view2);
                Button okButton = (Button)view2.findViewById(R.id.okButton);
                final AlertDialog dialog = builder.create();
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });







        if (lvChosen instanceof DragNDropListView) {
            ((DragNDropListView) lvChosen).setDropListener(mDropListener);
            ((DragNDropListView) lvChosen).setRemoveListener(mRemoveListener);
            ((DragNDropListView) lvChosen).setDragListener(mDragListener);
        }
        setListenerToRootView();
    }

    public void searchAct() {
        hideKeyboard();
        saveChanges(getSharedPreferences("UserPreference", 0));

        showProgress(true);
        (new CheckToken()).execute();
    }

    private String unreadCountText(int count) {
        int cn1 = count % 100;
        int cn2 = count % 10;
        if (cn1 > 10 && cn1 < 19) {
            return "записей";
        }
        if (cn2 == 1) return "запись";
        if (cn2 == 0) return "записей";
        if (cn2 > 4) return "записей";
        return "записи";

    }


    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}


