package com.logistic.paperrose.mttp.oldversion.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;


import com.logistic.paperrose.mttp.oldversion.LoginActivity;
import com.logistic.paperrose.mttp.oldversion.MainActivity;
import com.logistic.paperrose.mttp.oldversion.dragndrop.DragListener;
import com.logistic.paperrose.mttp.oldversion.dragndrop.DragNDropListView;
import com.logistic.paperrose.mttp.oldversion.dragndrop.DropListener;
import com.logistic.paperrose.mttp.oldversion.dragndrop.RemoveListener;
import com.logistic.paperrose.mttp.oldversion.pushes.MyActivity;
import com.logistic.paperrose.mttp.oldversion.results.SearchResults;
import com.logistic.paperrose.mttp.oldversion.results.ViewPagerSearchActivity;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.utils.ExtendedEditText;
import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.utils.JSONParser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;


public class PushSettingsActivityVer2 extends ActionBarActivity implements ActionBar.OnNavigationListener {

    ChosenFieldsAdapter adapter;
    ChosenFieldsAdapterVer2 adapterChosen;

    ArrayList<TripleTableField> tempTableFields;
    ArrayList<TripleTableField> tempPushFields;
    ArrayList<TripleTableField> tempSingleFields;

    @Override
    public void onBackPressed() {
        loadSettings();
        changes = CheckChanges();
        Toast.makeText(getApplicationContext(), Integer.toString(changes), Toast.LENGTH_LONG).show();
        if (changes > 0)
            showDialog(1);
        else
            super.onBackPressed();
    }

    private int CheckChanges() {
        int c = 0;
        boolean b = false;
        if (ApplicationParameters.chosenSearchResultSingleFields.size() != tempSingleFields.size()) {
            b = true;
            c += 4;
        }
        if (ApplicationParameters.chosenSearchResultsTableFields.size() != tempTableFields.size()) {
            b = true;
            c += 2;
        }
        if (ApplicationParameters.chosenTableFields.size() != tempPushFields.size()) {
            b = true;
            c += 1;
        }
        if (b) return c;
        for (int i = 0; i < tempPushFields.size(); i++) {
            if (!tempPushFields.get(i).getName().equals(ApplicationParameters.chosenTableFields.get(i).getName())) {
                c += 1;
                break;
            }
        }
        for (int i = 0; i < tempTableFields.size(); i++) {
            if (!tempTableFields.get(i).getName().equals(ApplicationParameters.chosenSearchResultsTableFields.get(i).getName())) {
                c += 2;
                break;
            }
        }
        for (int i = 0; i < tempSingleFields.size(); i++) {
            if (!tempSingleFields.get(i).getName().equals(ApplicationParameters.chosenSearchResultSingleFields.get(i).getName())) {
                c += 4;
                break;
            }
        }
        return c;
    }

    int changes = 0;

    protected Dialog onCreateDialog(int id) {
        if (id == 1) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            // заголовок
            adb.setTitle("Предупреждение");
            // сообщение
            adb.setMessage("Сохранить изменения перед выходом?");
            // иконка
            adb.setIcon(android.R.drawable.ic_dialog_info);
            // кнопка положительного ответа
            adb.setPositiveButton("Сохранить", myClickListener);
            adb.setNegativeButton("Не сохранять", myClickListener);
            // кнопка отрицательного ответа
            adb.setNeutralButton("Отмена", myClickListener);
            // кнопка нейтрального ответа
            // создаем диалог
            return adb.create();
        }
        return super.onCreateDialog(id);
    }
    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                // положительная кнопка
                case Dialog.BUTTON_POSITIVE:
                    adapterChosen.saveChanges(getSharedPreferences("UserPreference", 0));

                    (new CheckToken()).execute();
                    break;
                // нейтральная кнопка
                case Dialog.BUTTON_NEGATIVE:
                    ApplicationParameters.chosenTableFields = tempPushFields;
                    ApplicationParameters.chosenSearchResultsTableFields = tempTableFields;
                    ApplicationParameters.chosenSearchResultSingleFields = tempSingleFields;
                    finish();
                    break;
                case Dialog.BUTTON_NEUTRAL:
                    break;
            }
        }
    };
    private void loadSettings() {
        SharedPreferences historyPrefs = getSharedPreferences("UserPreference", 0);
        Gson gson = new Gson();
        String jsonH = historyPrefs.getString("all", "");
        String jsonS = historyPrefs.getString("selected", "");
        String jsonT = historyPrefs.getString("table_view", "");
        String jsonV = historyPrefs.getString("single_view", "");
        Type type = new TypeToken<ArrayList<TripleTableField>>(){}.getType();
        if (jsonH != "") {
            ApplicationParameters.tableFields = gson.fromJson(jsonH, type);
        }
        if (ApplicationParameters.tableFields.size() == 0) ApplicationParameters.testTableFields();
        if (jsonS == "") {
            ApplicationParameters.chosenTableFields = new ArrayList<TripleTableField>(ApplicationParameters.tableFields);
        } else {
            ApplicationParameters.chosenTableFields = gson.fromJson(jsonS, type);
        }
        if (jsonT == "") {
            ApplicationParameters.chosenSearchResultsTableFields = new ArrayList<TripleTableField>(ApplicationParameters.tableFields);
        } else {
            ApplicationParameters.chosenSearchResultsTableFields = gson.fromJson(jsonT, type);
        }
        if (jsonV == "") {
            ApplicationParameters.chosenSearchResultSingleFields = new ArrayList<TripleTableField>(ApplicationParameters.tableFields);
        } else {
            ApplicationParameters.chosenSearchResultSingleFields = gson.fromJson(jsonV, type);
        }
    }

    private void loadSettings(String set_type) {
        SharedPreferences historyPrefs = getSharedPreferences("UserPreference", 0);
        Gson gson = new Gson();
        String jsonH = historyPrefs.getString("all", "");
        String jsonS = historyPrefs.getString(set_type, "");
        Type type = new TypeToken<ArrayList<TripleTableField>>(){}.getType();
        if (jsonH != "") {
            ApplicationParameters.tableFields = gson.fromJson(jsonH, type);
        }
        if (ApplicationParameters.tableFields.size() == 0) ApplicationParameters.testTableFields();
        if (set_type.equals("selected")) {
            if (jsonS == "") {
                ApplicationParameters.chosenTableFields = new ArrayList<TripleTableField>();
            } else {
                ApplicationParameters.chosenTableFields = gson.fromJson(jsonS, type);
            }
        }
        if (set_type.equals("table_view")) {

            if (jsonS == "") {
                ApplicationParameters.chosenSearchResultsTableFields = new ArrayList<TripleTableField>();
            } else {
                ApplicationParameters.chosenSearchResultsTableFields = gson.fromJson(jsonS, type);
            }
        }
        if (set_type.equals("single_view")) {

            if (jsonS == "") {
                ApplicationParameters.chosenSearchResultSingleFields = new ArrayList<TripleTableField>();
            } else {
                ApplicationParameters.chosenSearchResultSingleFields = gson.fromJson(jsonS, type);
            }
        }
    }

    @Override
    protected void onDestroy() {
       // ;

        adapterChosen.saveChanges(getSharedPreferences("UserPreference", 0));
        super.onDestroy();
    }

    private void saveCredentials(String hash, String key) {
        SharedPreferences mPrefs = getSharedPreferences("UserPreference", 0);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(key, hash);
        editor.commit();
    }

    @Override
    public boolean onNavigationItemSelected(int i, long l) {
        switch (i) {
            case 0:
                refreshAdapters(ApplicationParameters.chosenTableFields, "selected");
                break;
            case 1:
                refreshAdapters(ApplicationParameters.chosenSearchResultsTableFields, "table_view");
                break;
            case 2:
                refreshAdapters(ApplicationParameters.chosenSearchResultSingleFields, "single_view");
                break;
        }
        return false;
    }

    protected class CheckToken extends AsyncTask<Pair<String, HashMap<String, String>>, Void, JSONObject> {
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
            try {
                if (res == null) {
                    Toast.makeText(PushSettingsActivityVer2.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
                } else
                if (res.has("error")) {
                    if (res.getInt("status_code") == 401) {
                        Intent intent = new Intent(PushSettingsActivityVer2.this, LoginActivity.class);
                        intent.putExtra("login", true);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), res.getString("error"), Toast.LENGTH_LONG).show();
                    }
                } else {
                    saveCredentials(res.getString("access_token"), "access_token");

                    if (ApplicationParameters.activity != null) {
                        MainActivity.saveChanges(getApplicationContext().getSharedPreferences("UserPreference", 0));
                    }
                    loadSettings();
                    if (tabNum == 1 && changes % 4 > 1) {
                        ApplicationParameters.changed = true;
                        (new CheckToken2()).execute();
                    } else if (tabNum == 2 && changes > 3) {
                        if (changes > 5) {
                            ApplicationParameters.changed = true;
                            (new CheckToken2()).execute();
                        } else {
                            (new CheckToken2()).execute();
                        }
                    } else if (tabNum == 0 && changes % 2 > 0) {
                        ((MyActivity)ApplicationParameters.activity).refreshHistory();
                        finish();
                    } else {
                        finish();
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
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
                Toast.makeText(PushSettingsActivityVer2.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
            } else
            if (res.has("error")) {
                Intent intent = new Intent(PushSettingsActivityVer2.this, LoginActivity.class);
                intent.putExtra("login", true);
                startActivity(intent);
                finish();
            } else {
                try {
                    saveCredentials(res.getString("access_token"), "access_token");
                    if (tabNum == 1 && changes % 4 > 1) {
                        ((SearchResults) ApplicationParameters.activity).jsonResults = new JSONArray(res.getString("result"));
                    } else if (tabNum == 2 && changes > 3) {
                        if (changes > 5) {
                            ((ViewPagerSearchActivity) ApplicationParameters.activity).refresh(new JSONArray(res.getString("result")));
                            ((ViewPagerSearchActivity) ApplicationParameters.activity).parentActivity.jsonResults = new JSONArray(res.getString("result"));
                        } else {
                            ((ViewPagerSearchActivity) ApplicationParameters.activity).refresh(new JSONArray(res.getString("result")));
                        }
                        ApplicationParameters.activity = ((ViewPagerSearchActivity) ApplicationParameters.activity).parentActivity;
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //return true;
            }
        }
    }


    public String getPref(String key) {
        return getSharedPreferences("UserPreference", 0).getString(key, "123");
    }

    public String getFieldsString(ArrayList<TripleTableField> fields) {
        String cs = "";
        if (fields.size() > 0) cs = fields.get(0).getName();
        for (int i = 1; i < fields.size(); i++) {
            cs += "," + fields.get(i).getName();
        }
        return cs;
    }

    public JSONObject checkServerCredentials() throws JSONException {
        final String access_token = getPref("access_token");
        final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
        JSONParser parser = new JSONParser();
        JSONObject obj = parser.getJSONFromUrl("http://"+ApplicationParameters.MAIN_DOMAIN +"/user/request_fields", new HashMap<String, String>() {{
            put("access_token", access_token);
            put("hs", secure_token);
            put("fields", getFieldsString(ApplicationParameters.chosenSearchResultSingleFields));
            put("push_fields", getFieldsString(ApplicationParameters.chosenTableFields));
            put("table_fields", getFieldsString(ApplicationParameters.chosenSearchResultsTableFields));
        }}, getApplicationContext());
        return obj;
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
                    ImageView iv = (ImageView)itemView.findViewById(R.id.upItem);
                    if (iv != null) iv.setVisibility(View.VISIBLE);
                }

                public void onStopDrag(View itemView) {
                    itemView.setVisibility(View.VISIBLE);
                    itemView.setBackgroundColor(defaultBackgroundColor);
                    ImageView iv = (ImageView)itemView.findViewById(R.id.upItem);
                    if (iv != null) iv.setVisibility(View.VISIBLE);
                }

            };

    DragNDropListView lvChosen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
       // this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_push_settings_ver2);

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
        refreshAdapters(ApplicationParameters.chosenTableFields, "selected");

        if (lvChosen instanceof DragNDropListView) {
            ((DragNDropListView) lvChosen).setDropListener(mDropListener);
            ((DragNDropListView) lvChosen).setRemoveListener(mRemoveListener);
            ((DragNDropListView) lvChosen).setDragListener(mDragListener);
        }

        ActionBar ab = getSupportActionBar();
        //ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0000ff"))); not changing the tab color
        //ab.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#0000ff")));
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ColorDrawable colorDrawable = new ColorDrawable(getResources().getColor(R.color.secondary_color));
        ab.setBackgroundDrawable(colorDrawable);
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        ActionBar.Tab tab = ab.newTab()
                .setText("")
                .setIcon(getResources().getDrawable(R.drawable.pushes_small))

                        // .setCustomView(tabView)
                .setTabListener(
                        new MyTabListener(new Refresher(ApplicationParameters.chosenTableFields, "selected")) );
        ab.addTab( tab );
        tab = ab.newTab()
                .setText("")
                .setIcon(getResources().getDrawable(R.drawable.search_results_white_small))
                        // .setCustomView(tabView)
                .setTabListener(
                        new MyTabListener(new Refresher(ApplicationParameters.chosenSearchResultsTableFields, "table_view")));
        ab.addTab( tab );
        tab = ab.newTab()
                .setText("")
               // .setCustomView(tabView)
                .setIcon(getResources().getDrawable(R.drawable.single_record_white_small))

                .setTabListener(
                        new MyTabListener(new Refresher(ApplicationParameters.chosenSearchResultSingleFields, "single_view")));
        ab.addTab( tab );
        tabNum = getIntent().getIntExtra("tab", -1);
        ab.setSelectedNavigationItem(tabNum > -1 ? tabNum : 0);
        tempPushFields = new ArrayList<TripleTableField>(ApplicationParameters.chosenTableFields);
        tempTableFields = new ArrayList<TripleTableField>(ApplicationParameters.chosenSearchResultsTableFields);
        tempSingleFields = new ArrayList<TripleTableField>(ApplicationParameters.chosenSearchResultSingleFields);
    }
    Integer tabNum;
    private class Refresher {
        private ArrayList<TripleTableField> secondaryList;
        private String saveType;
        Refresher(ArrayList<TripleTableField> lst, String saveType) {
            secondaryList = lst;
            this.saveType = saveType;
        }
        public void refresh() {
            refreshAdapters(secondaryList, saveType);
        }

    }
    public void refreshAdapters(ArrayList<TripleTableField> secondaryList, String saveType) {
        if (adapterChosen != null)
            adapterChosen.saveChanges(getSharedPreferences("UserPreference", 0));
        ArrayList<TripleTableField> used = new ArrayList<TripleTableField>();
        ListView lvMain = (ListView) findViewById(R.id.pushFieldsList);
        lvChosen = (DragNDropListView) findViewById(R.id.chosenPushFieldsList);
        adapter = new ChosenFieldsAdapter(this, R.layout.activity_push_settings_ver2, ApplicationParameters.tableFields, lvMain, ApplicationParameters.tableFields, secondaryList);
        adapter.setUsedObjects(used);
        adapterChosen = new ChosenFieldsAdapterVer2(this, R.layout.activity_push_settings_ver2, used, lvChosen, secondaryList);
        adapterChosen.saveType = saveType;
        adapterChosen.setChosen(true);
        adapterChosen.firstListAdapter = adapter;
        adapter.secondListAdapter = adapterChosen;

        lvMain.setAdapter(adapter);

        lvChosen.setAdapter(adapterChosen);
    }
    private class MyTabListener implements ActionBar.TabListener {

        Refresher rf;
        MyTabListener(Refresher rf0) {
            rf = rf0;
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            loadSettings();
            rf.refresh();
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present

        MenuItem mi2 = menu.add(0, 1, 0, "Save settings");
        mi2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                adapterChosen.saveChanges(getSharedPreferences("UserPreference", 0));

                (new CheckToken()).execute();

                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
