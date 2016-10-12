package com.logistic.paperrose.mttp.oldversion.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.logistic.paperrose.mttp.oldversion.BaseLogisticActivity;
import com.logistic.paperrose.mttp.oldversion.LoginActivity;
import com.logistic.paperrose.mttp.oldversion.MainActivity;
import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.clients.SettingsType;
import com.logistic.paperrose.mttp.oldversion.dragndrop.DragListener;
import com.logistic.paperrose.mttp.oldversion.dragndrop.DragNDropListView;
import com.logistic.paperrose.mttp.oldversion.dragndrop.DropListener;
import com.logistic.paperrose.mttp.oldversion.dragndrop.RemoveListener;
import com.logistic.paperrose.mttp.oldversion.pushes.MyActivity;
import com.logistic.paperrose.mttp.oldversion.results.BaseSearchResults;
import com.logistic.paperrose.mttp.oldversion.results.BaseViewPagerSearchActivity;
import com.logistic.paperrose.mttp.oldversion.results.SearchResults;
import com.logistic.paperrose.mttp.oldversion.results.ViewPagerSearchActivity;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.utils.ExtendedEditText;
import com.logistic.paperrose.mttp.oldversion.utils.JSONParser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;


public class PushSettingsActivity extends BaseLogisticActivity {


    int bookmarkID = 0;
    SettingsType type;
    ChosenFieldsAdapter adapter;
    AlertFieldsAdapter alertAdapter;
    ChosenFieldsAdapterVer2 adapterChosen;
    ArrayList<TripleTableField> tempFields;
    int changes = 0;
    DragNDropListView lvChosen;


    @Override
    public void onBackPressed() {
        loadSettings();
        changes = CheckChanges();
        //Toast.makeText(getApplicationContext(), Integer.toString(changes), Toast.LENGTH_LONG).show();
        if (changes > 0)
            showDialog(1);
        else
            super.onBackPressed();
    }

    public boolean ActChanges() {
        loadSettings();
        changes = CheckChanges();
        //Toast.makeText(getApplicationContext(), Integer.toString(changes), Toast.LENGTH_LONG).show();
        if (changes > 0)
            showDialog(1);
        return changes > 0;
    }

    private int CheckChanges() {
        int c = 0;
        switch (type) {
            case PUSH:
                if (ApplicationParameters.chosenTableFields.size() != tempFields.size()) {
                    return 1;
                }
                for (int i = 0; i < tempFields.size(); i++) {
                    if (!tempFields.get(i).getName().equals(ApplicationParameters.chosenTableFields.get(i).getName())) {
                        return 1;
                    }
                }
                break;
            case TABLE:
                if (ApplicationParameters.chosenSearchResultsTableFields.size() != tempFields.size()) {
                    return 2;
                }
                for (int i = 0; i < tempFields.size(); i++) {
                    if (!tempFields.get(i).getName().equals(ApplicationParameters.chosenSearchResultsTableFields.get(i).getName())) {
                        return 2;
                    }
                }
                break;
            case SINGLE:
                if (ApplicationParameters.chosenSearchResultSingleFields.size() != tempFields.size()) {
                    return 4;
                }
                for (int i = 0; i < tempFields.size(); i++) {
                    if (!tempFields.get(i).getName().equals(ApplicationParameters.chosenSearchResultSingleFields.get(i).getName())) {
                        return 4;
                    }
                }
                break;
            case DEC_TABLE:
                if (ApplicationParameters.chosenDeclarationFields.size() != tempFields.size()) {
                    return 2;
                }
                for (int i = 0; i < tempFields.size(); i++) {
                    if (!tempFields.get(i).getName().equals(ApplicationParameters.chosenDeclarationFields.get(i).getName())) {
                        return 2;
                    }
                }
                break;
            case DEC_SINGLE:
                if (ApplicationParameters.chosenDeclarationSingleFields.size() != tempFields.size()) {
                    return 4;
                }
                for (int i = 0; i < tempFields.size(); i++) {
                    if (!tempFields.get(i).getName().equals(ApplicationParameters.chosenDeclarationSingleFields.get(i).getName())) {
                        return 4;
                    }
                }
                break;
            case REP_DEC_TABLE:
                if (ApplicationParameters.chosenDeclarationReportsFields.size() != tempFields.size()) {
                    return 2;
                }
                for (int i = 0; i < tempFields.size(); i++) {
                    if (!tempFields.get(i).getName().equals(ApplicationParameters.chosenDeclarationReportsFields.get(i).getName())) {
                        return 2;
                    }
                }
                break;
            case REP_DEC_SINGLE:
                if (ApplicationParameters.chosenDeclarationReportsSingleFields.size() != tempFields.size()) {
                    return 4;
                }
                for (int i = 0; i < tempFields.size(); i++) {
                    if (!tempFields.get(i).getName().equals(ApplicationParameters.chosenDeclarationReportsSingleFields.get(i).getName())) {
                        return 4;
                    }
                }
                break;
            default: break;
        }

        return 0;
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

    public void refreshAdapters(final ArrayList<TripleTableField> secondaryList, String saveType) {
        if (adapterChosen != null)
            adapterChosen.saveChanges(getSharedPreferences("UserPreference", 0));
        ArrayList<TripleTableField> used = new ArrayList<TripleTableField>();
        ListView lvMain = (ListView) findViewById(R.id.pushFieldsList);

        lvChosen = (DragNDropListView) findViewById(R.id.chosenPushFieldsList);
        if (type == SettingsType.DEC_TABLE || type == SettingsType.DEC_SINGLE)
            adapter = new ChosenFieldsAdapter(this, R.layout.activity_push_settings_ver2, ApplicationParameters.declarationFields, lvMain, ApplicationParameters.declarationFields, secondaryList);

        else
            adapter = new ChosenFieldsAdapter(this, R.layout.activity_push_settings_ver2, ApplicationParameters.tableFields, lvMain, ApplicationParameters.tableFields, secondaryList);
        adapter.sType = type;
        adapter.setUsedObjects(used);
        adapterChosen = new ChosenFieldsAdapterVer2(this, R.layout.activity_push_settings_ver2, used, lvChosen, secondaryList);
        adapterChosen.saveType = saveType;
        adapterChosen.setChosen(true);
        adapterChosen.firstListAdapter = adapter;
        adapter.secondListAdapter = adapterChosen;

        lvMain.setAdapter(adapter);

        lvChosen.setAdapter(adapterChosen);
        if (lvChosen instanceof DragNDropListView) {
            ((DragNDropListView) lvChosen).setDropListener(mDropListener);
            ((DragNDropListView) lvChosen).setRemoveListener(mRemoveListener);
            ((DragNDropListView) lvChosen).setDragListener(mDragListener);
        }
        Button alertButton = (Button)findViewById(R.id.addFields);
        alertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LinearLayout view2 = (LinearLayout) getLayoutInflater()
                        .inflate(R.layout.custom_dialog, null);

                lvMain2 = (ListView)view2.findViewById(R.id.alertFieldsList);
                if (type == SettingsType.DEC_TABLE || type == SettingsType.DEC_SINGLE) {
                    alertAdapter = new AlertFieldsAdapter(PushSettingsActivity.this, R.layout.activity_push_settings_ver2, ApplicationParameters.declarationFields, lvMain2, ApplicationParameters.declarationFields, secondaryList);
                    alertAdapter.type = 1;
                }
                else {
                    alertAdapter = new AlertFieldsAdapter(PushSettingsActivity.this, R.layout.activity_push_settings_ver2, ApplicationParameters.tableFields, lvMain2, ApplicationParameters.tableFields, secondaryList);
                    alertAdapter.type = 0;
                }
                alertAdapter.secondListAdapter = adapterChosen;
                lvMain2.setAdapter(alertAdapter);

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
                AlertDialog.Builder builder = new AlertDialog.Builder(PushSettingsActivity.this);
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
    }

    ListView lvMain2;

    private void loadSettings() {
        SharedPreferences historyPrefs = getSharedPreferences("UserPreference", 0);
        Gson gson = new Gson();
        String jsonH = historyPrefs.getString("all", "");
        String jsonDec = historyPrefs.getString("declarations", "");
        String jsonS = historyPrefs.getString("selected", "");
        String jsonT = historyPrefs.getString("table_view", "");
        String jsonV = historyPrefs.getString("single_view", "");
        String jsonDecT = historyPrefs.getString("dec_table_view", "");
        String jsonDecV = historyPrefs.getString("dec_single_view", "");
        String jsonDecRepT = historyPrefs.getString("rep_dec_table_view", "");
        String jsonDecRepV = historyPrefs.getString("rep_dec_single_view", "");
        Type type = new TypeToken<ArrayList<TripleTableField>>(){}.getType();
        if (jsonH != "") {
            ApplicationParameters.tableFields = gson.fromJson(jsonH, type);
        }
        //if (ApplicationParameters.tableFields.size() == 0) ApplicationParameters.testTableFields();
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
        if (jsonDecT == "") {
            ApplicationParameters.chosenDeclarationFields = new ArrayList<TripleTableField>(ApplicationParameters.declarationFields);
        } else {
            ApplicationParameters.chosenDeclarationFields = gson.fromJson(jsonDecT, type);
        }
        if (jsonDecV == "") {
            ApplicationParameters.chosenDeclarationSingleFields = new ArrayList<TripleTableField>(ApplicationParameters.declarationFields);
        } else {
            ApplicationParameters.chosenDeclarationSingleFields = gson.fromJson(jsonDecV, type);
        }
        if (jsonDecRepT == "") {
            ApplicationParameters.chosenDeclarationReportsFields = new ArrayList<TripleTableField>(ApplicationParameters.declarationFields);
        } else {
            ApplicationParameters.chosenDeclarationReportsFields = gson.fromJson(jsonDecRepT, type);
        }
        if (jsonDecRepV == "") {
            ApplicationParameters.chosenDeclarationReportsSingleFields = new ArrayList<TripleTableField>(ApplicationParameters.declarationFields);
        } else {
            ApplicationParameters.chosenDeclarationReportsSingleFields = gson.fromJson(jsonDecRepV, type);
        }
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
                    Toast.makeText(PushSettingsActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
                } else
                if (res.has("error")) {
                    if (res.getInt("status_code") == 401) {
                        Intent intent = new Intent(PushSettingsActivity.this, LoginActivity.class);
                        intent.putExtra("login", true);
                        startActivity(intent);
                        finish();
                    } else {
                        showProgress(false);
                        Toast.makeText(getApplicationContext(), res.getString("error"), Toast.LENGTH_LONG).show();
                    }
                } else {
                    saveCredentials(res.getString("access_token"), "access_token");
                    Log.e("token", res.getString("access_token"));
                    if (ApplicationParameters.activity != null) {
                        MainActivity.saveChanges(getApplicationContext().getSharedPreferences("UserPreference", 0));
                    }
                    loadSettings();
                    if (type == SettingsType.TABLE && changes % 4 > 1 && (ApplicationParameters.activity instanceof SearchResults)) {
                        ApplicationParameters.changed = true;
                        (new CheckToken2()).execute();
                    } else if (type == SettingsType.SINGLE && changes > 3 && (ApplicationParameters.activity instanceof ViewPagerSearchActivity)) {
                        if (changes > 5) {
                            ApplicationParameters.changed = true;
                            (new CheckToken2()).execute();
                        } else {
                            (new CheckToken2()).execute();
                        }
                    } else if (type == SettingsType.DEC_TABLE && changes % 4 > 1 && (ApplicationParameters.activity instanceof BaseSearchResults)) {
                        ApplicationParameters.changed = true;
                        finish();
                        //(new CheckToken2()).execute();
                    } else if (type == SettingsType.DEC_SINGLE && changes > 3 && (ApplicationParameters.activity instanceof BaseViewPagerSearchActivity)) {
                        ApplicationParameters.changed = true;
                        finish();
                    }
                    else if (type == SettingsType.PUSH && changes % 2 > 0 && (ApplicationParameters.activity instanceof MyActivity)) {
                        ((MyActivity) ApplicationParameters.activity).refreshHistory();
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
                    showProgress(true);
                    (new CheckToken()).execute();
                    break;
                // нейтральная кнопка
                case Dialog.BUTTON_NEGATIVE:
                    switch (type) {
                        case PUSH:
                            ApplicationParameters.chosenTableFields = tempFields;
                            break;
                        case TABLE:
                            ApplicationParameters.chosenSearchResultsTableFields = tempFields;
                            break;
                        case SINGLE:
                            ApplicationParameters.chosenSearchResultSingleFields = tempFields;
                            break;
                        case DEC_TABLE:
                            ApplicationParameters.chosenDeclarationFields = tempFields;
                            break;
                        case DEC_SINGLE:
                            ApplicationParameters.chosenDeclarationSingleFields = tempFields;
                            break;
                        case REP_DEC_TABLE:
                            ApplicationParameters.chosenDeclarationReportsFields = tempFields;
                            break;
                        case REP_DEC_SINGLE:
                            ApplicationParameters.chosenDeclarationReportsSingleFields = tempFields;
                            break;
                        default: break;
                    }



                    finish();
                    break;
                case Dialog.BUTTON_NEUTRAL:
                    break;
            }
        }
    };
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
            if (res.has("error")) {
                Intent intent = new Intent(PushSettingsActivity.this, LoginActivity.class);
                intent.putExtra("login", true);
                startActivity(intent);
                finish();
            } else {
                try {
                    if (res == null) {
                        Toast.makeText(PushSettingsActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
                    } else
                        saveCredentials(res.getString("access_token"), "access_token");
                    showProgress(false);
                    if (type == SettingsType.TABLE && changes % 4 > 1) {
                        ((SearchResults) ApplicationParameters.activity).jsonResults = new JSONArray(res.getString("result"));

                    } else if (type == SettingsType.SINGLE && changes > 3) {
                        try {
                            if (changes > 5) {
                                ((ViewPagerSearchActivity) ApplicationParameters.activity).refresh(new JSONArray(res.getString("result")));
                                ((ViewPagerSearchActivity) ApplicationParameters.activity).parentActivity.jsonResults = new JSONArray(res.getString("result"));
                            } else {
                                ((ViewPagerSearchActivity) ApplicationParameters.activity).refresh(new JSONArray(res.getString("result")));
                            }
                            ApplicationParameters.activity = ((ViewPagerSearchActivity) ApplicationParameters.activity).parentActivity;
                        } catch (Exception e) {

                        }

                    }
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //return true;
            }
        }
    }


    public JSONObject checkServerCredentials() throws JSONException {
        final String access_token = getPref("access_token");
        final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
        JSONParser parser = new JSONParser();
        JSONObject obj = parser.getJSONFromUrl("http://"+ApplicationParameters.MAIN_DOMAIN +"/user/request_fields", new HashMap<String, String>() {{
            put("access_token", access_token);
            put("hs", secure_token);
            ArrayList<TripleTableField> fff = new ArrayList<TripleTableField>();

            if (type == SettingsType.DEC_SINGLE || type == SettingsType.DEC_TABLE) {
                put("table_id", "2");
                fff.addAll(ApplicationParameters.chosenDeclarationFields);
                fff.addAll(ApplicationParameters.chosenDeclarationSingleFields);
            }  else if (type == SettingsType.REP_DEC_SINGLE || type == SettingsType.REP_DEC_TABLE) {
                    put("table_id", "2");
                    fff.addAll(ApplicationParameters.chosenDeclarationReportsFields);
                    fff.addAll(ApplicationParameters.chosenDeclarationReportsSingleFields);
            } else {
                fff.addAll(ApplicationParameters.chosenSearchResultSingleFields);
                fff.addAll(ApplicationParameters.chosenSearchResultsTableFields);
            }
            put("fields", getFieldsString(fff));
            put("push_fields", getFieldsString(ApplicationParameters.chosenSearchResultSingleFields));
            put("table_fields", getFieldsString(ApplicationParameters.chosenSearchResultsTableFields));
            put("dec_table_fields", getFieldsString(ApplicationParameters.chosenDeclarationFields));
            put("dec_single_fields", getFieldsString(ApplicationParameters.chosenDeclarationSingleFields));
            put("rep_dec_table_fields", getFieldsString(ApplicationParameters.chosenDeclarationReportsFields));
            put("rep_dec_single_fields", getFieldsString(ApplicationParameters.chosenDeclarationReportsSingleFields));
            if (bookmarkID > 0) {
                put("bookmark_id", Integer.toString(bookmarkID));
            }
        }}, getApplicationContext());
        return obj;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        type = SettingsType.values()[getIntent().getIntExtra("adapterType", 1)];
        bookmarkID = getIntent().getIntExtra("bookmarkID", 0);
        switch (type) {
            case BOOKMARK:
                break;
            case MAIN:
                break;
            case SINGLE:
                tempFields = new ArrayList<TripleTableField>(ApplicationParameters.chosenSearchResultSingleFields);
                setLayoutId(R.layout.activity_push_settings_ver2);
                super.onCreate(savedInstanceState);
                refreshAdapters(ApplicationParameters.chosenSearchResultSingleFields, "single_view");
                break;
            case TABLE:
                tempFields = new ArrayList<TripleTableField>(ApplicationParameters.chosenSearchResultsTableFields);
                setLayoutId(R.layout.activity_push_settings_ver2);
                super.onCreate(savedInstanceState);
                refreshAdapters(ApplicationParameters.chosenSearchResultsTableFields, "table_view");
                break;
            case DEC_SINGLE:
                tempFields = new ArrayList<TripleTableField>(ApplicationParameters.chosenDeclarationSingleFields);
                setLayoutId(R.layout.activity_push_settings_ver2);
                super.onCreate(savedInstanceState);
                refreshAdapters(ApplicationParameters.chosenDeclarationSingleFields, "dec_single_view");
                break;
            case DEC_TABLE:
                tempFields = new ArrayList<TripleTableField>(ApplicationParameters.chosenDeclarationFields);
                setLayoutId(R.layout.activity_push_settings_ver2);
                super.onCreate(savedInstanceState);
                refreshAdapters(ApplicationParameters.chosenDeclarationFields, "dec_table_view");
                break;
            case REP_DEC_SINGLE:
                tempFields = new ArrayList<TripleTableField>(ApplicationParameters.chosenDeclarationReportsSingleFields);
                setLayoutId(R.layout.activity_push_settings_ver2);
                super.onCreate(savedInstanceState);
                refreshAdapters(ApplicationParameters.chosenDeclarationReportsSingleFields, "rep_dec_single_view");
                break;
            case REP_DEC_TABLE:
                tempFields = new ArrayList<TripleTableField>(ApplicationParameters.chosenDeclarationReportsFields);
                setLayoutId(R.layout.activity_push_settings_ver2);
                super.onCreate(savedInstanceState);
                refreshAdapters(ApplicationParameters.chosenDeclarationReportsFields, "rep_dec_table_view");
                break;
            case PUSH:
                tempFields = new ArrayList<TripleTableField>(ApplicationParameters.chosenTableFields);
                setLayoutId(R.layout.activity_push_settings_ver2);
                super.onCreate(savedInstanceState);
                refreshAdapters(ApplicationParameters.chosenTableFields, "selected");
                break;
            default: break;
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

    }

    private DropListener mDropListener =
            new DropListener() {
                public void onDrop(int from, int to) {
                    if (adapterChosen instanceof ChosenFieldsAdapterVer2) {
                      //  TripleTableField temp =  tempFields.get(from);
                       // tempFields.remove(from);
                       // tempFields.add(to, temp);
                        adapterChosen.onDrop(from, to);
                        lvChosen.invalidateViews();
                        adapterChosen.notifyDataSetChanged();

                    }
                }
            };

    private RemoveListener mRemoveListener =
            new RemoveListener() {
                public void onRemove(int which) {
                    if (adapterChosen instanceof ChosenFieldsAdapterVer2) {
                       // if (which < 0 || which > tempFields.size()-1) return;
                       // tempFields.remove(which);
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

}
