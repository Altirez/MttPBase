package com.logistic.paperrose.mttp.oldversion;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.clients.SettingsType;
import com.logistic.paperrose.mttp.oldversion.dragndrop.DragListener;
import com.logistic.paperrose.mttp.oldversion.dragndrop.DragNDropListView;
import com.logistic.paperrose.mttp.oldversion.dragndrop.DropListener;
import com.logistic.paperrose.mttp.oldversion.dragndrop.RemoveListener;
import com.logistic.paperrose.mttp.oldversion.list.BaseChosenListAdapter;
import com.logistic.paperrose.mttp.oldversion.list.FieldsListAdapter;
import com.logistic.paperrose.mttp.oldversion.list.SettingsChosenListAdapter;
import com.logistic.paperrose.mttp.oldversion.results.BaseReportsSearchResults;
import com.logistic.paperrose.mttp.oldversion.results.BaseReportsViewPagerSearchActivity;
import com.logistic.paperrose.mttp.oldversion.results.BaseSearchResults;
import com.logistic.paperrose.mttp.oldversion.results.BaseViewPagerSearchActivity;
import com.logistic.paperrose.mttp.oldversion.results.SearchResults;
import com.logistic.paperrose.mttp.oldversion.results.ViewPagerSearchActivity;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;
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


public class SettingsActivity extends BaseLogisticActivity {
    int bookmarkID = 0;
    String saveString;
    SettingsType type;
    BaseChosenListAdapter adapter;
    FieldsListAdapter alertAdapter;
    ArrayList<TripleTableField> allFields = new ArrayList<TripleTableField>();
    ArrayList<TripleTableField> chosenFields = new ArrayList<TripleTableField>();
    ListView lvMain2;
    ListView lvMain;

    public boolean compareLists(ArrayList<TripleTableField> list1, ArrayList<TripleTableField> list2) {
        if (list1.size() != list2.size()) return true;
        for (int i = 0; i < list1.size(); i++) {
            if (!(list1.get(i).getName().equals(list2.get(i).getName()))) return true;
        }
        return false;
    }
    @Override
    public void onBackPressed() {
        if (checkChanges()) {
            showDialog(1);
        } else {
            super.onBackPressed();
        }
    }
    public boolean checkChanges() {
        switch (type) {
            case PUSH:
                return compareLists(ApplicationParameters.chosenTableFields, chosenFields);
            case BOOKMARK:
                break;
            case SINGLE:
                return compareLists(ApplicationParameters.chosenSearchResultSingleFields, chosenFields);
            case TABLE:
                return compareLists(ApplicationParameters.chosenSearchResultsTableFields, chosenFields);
            case DEC_SINGLE:
                return compareLists(ApplicationParameters.chosenDeclarationSingleFields, chosenFields);
            case DEC_TABLE:
                return compareLists(ApplicationParameters.chosenDeclarationFields, chosenFields);
            case REP_DEC_SINGLE:
                return compareLists(ApplicationParameters.chosenDeclarationReportsSingleFields, chosenFields);
            case REP_DEC_TABLE:
                return compareLists(ApplicationParameters.chosenDeclarationReportsFields, chosenFields);
            case DECLARATION:
                break;
            case MAIN:
                break;
        }
        return false;
    }

    private void loadSettings() {
        SharedPreferences historyPrefs = getSharedPreferences("UserPreference", 0);
        Gson gson = new Gson();
        String json = historyPrefs.getString(saveString, "");
        Type type2 = new TypeToken<ArrayList<TripleTableField>>(){}.getType();

        switch (type) {
            case PUSH:
                if (json == "") {
                    ApplicationParameters.chosenTableFields = new ArrayList<TripleTableField>(ApplicationParameters.tableFields);
                } else {
                    ApplicationParameters.chosenTableFields = gson.fromJson(json, type2);
                }
                break;
            case BOOKMARK:
                break;
            case SINGLE:
                if (json == "") {
                    ApplicationParameters.chosenSearchResultSingleFields = new ArrayList<TripleTableField>(ApplicationParameters.tableFields);
                } else {
                    ApplicationParameters.chosenSearchResultSingleFields = gson.fromJson(json, type2);
                }
                break;
            case TABLE:
                if (json == "") {
                    ApplicationParameters.chosenSearchResultsTableFields = new ArrayList<TripleTableField>(ApplicationParameters.tableFields);
                } else {
                    ApplicationParameters.chosenSearchResultsTableFields = gson.fromJson(json, type2);
                }
                break;
            case DEC_SINGLE:
                if (json == "") {
                    ApplicationParameters.chosenDeclarationSingleFields = new ArrayList<TripleTableField>(ApplicationParameters.declarationFields);
                } else {
                    ApplicationParameters.chosenDeclarationSingleFields = gson.fromJson(json, type2);
                }
                break;
            case DEC_TABLE:
                if (json == "") {
                    ApplicationParameters.chosenDeclarationFields = new ArrayList<TripleTableField>(ApplicationParameters.declarationFields);
                } else {
                    ApplicationParameters.chosenDeclarationFields = gson.fromJson(json, type2);
                }
                break;
            case REP_DEC_SINGLE:
                if (json == "") {
                    ApplicationParameters.chosenDeclarationReportsSingleFields = new ArrayList<TripleTableField>(ApplicationParameters.declarationFields);
                } else {
                    ApplicationParameters.chosenDeclarationReportsSingleFields = gson.fromJson(json, type2);
                }
                break;
            case REP_DEC_TABLE:
                if (json == "") {
                    ApplicationParameters.chosenDeclarationReportsFields = new ArrayList<TripleTableField>(ApplicationParameters.declarationFields);
                } else {
                    ApplicationParameters.chosenDeclarationReportsFields = gson.fromJson(json, type2);
                }
                break;
            case DECLARATION:
                break;
            case MAIN:
                break;
        }

    }

    public void saveSettings() {
        SharedPreferences historyPrefs = getSharedPreferences("UserPreference", 0);
        SharedPreferences.Editor prefsEditor = historyPrefs.edit();
        Gson gson = new Gson();
        String json2 = gson.toJson(chosenFields);
        prefsEditor.putString(saveString, json2);
        prefsEditor.commit();
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
                    saveSettings();
                    showProgress(true);
                    (new FieldsRequest()).execute();
                    break;
                // нейтральная кнопка
                case Dialog.BUTTON_NEGATIVE:
                    finish();
                    break;
                case Dialog.BUTTON_NEUTRAL:
                    break;
            }
        }
    };

    public String formOrderString(ArrayList<TripleTableField> tableOrder, ArrayList<TripleTableField> singleOrder) {
        String st1 = "";
        String st2 = "";
        if (tableOrder.size() > 0) {
            st1 = Integer.toString(tableOrder.get(0).id);
            for (int i = 1; i < tableOrder.size(); i++) {
                st1 += ">" + Integer.toString(tableOrder.get(i).id);
            }
        }
        if (singleOrder.size() > 0) {
            st2 = Integer.toString(singleOrder.get(0).id);
            for (int i = 1; i < singleOrder.size(); i++) {
                st2 += ">" + Integer.toString(singleOrder.get(i).id);
            }
        }
        return st1 + "<<<" + st2;
    }

    public JSONObject checkServerCredentials() throws JSONException {
        final String access_token = getPref("access_token");
        final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
        JSONParser parser = new JSONParser();
        loadSettings();
        JSONObject obj = parser.getJSONFromUrl("http://"+ApplicationParameters.MAIN_DOMAIN +"/user/request_fields", new HashMap<String, String>() {{
            put("access_token", access_token);
            put("hs", secure_token);
            ArrayList<TripleTableField> fff = new ArrayList<TripleTableField>();
            String order;
            if (type == SettingsType.DEC_SINGLE || type == SettingsType.DEC_TABLE) {
                put("table_id", "2");
                fff.addAll(ApplicationParameters.chosenDeclarationFields);
                fff.addAll(ApplicationParameters.chosenDeclarationSingleFields);
                order = formOrderString(ApplicationParameters.chosenDeclarationFields, ApplicationParameters.chosenDeclarationSingleFields);
            } else if (type == SettingsType.REP_DEC_SINGLE || type == SettingsType.REP_DEC_TABLE) {
                put("table_id", "3");
                fff.addAll(ApplicationParameters.chosenDeclarationReportsFields);
                fff.addAll(ApplicationParameters.chosenDeclarationReportsSingleFields);
                order = formOrderString(ApplicationParameters.chosenDeclarationReportsFields, ApplicationParameters.chosenDeclarationReportsSingleFields);
            } else {
                fff.addAll(ApplicationParameters.chosenSearchResultSingleFields);
                fff.addAll(ApplicationParameters.chosenSearchResultsTableFields);
                order = formOrderString(ApplicationParameters.chosenSearchResultsTableFields, ApplicationParameters.chosenSearchResultSingleFields);
            }
            put("fields", getFieldsString(fff));
            put("push_fields", getFieldsString(ApplicationParameters.chosenSearchResultSingleFields));
            put("table_fields", getFieldsString(ApplicationParameters.chosenSearchResultsTableFields));
            put("order", order);
            put("dec_table_fields", getFieldsString(ApplicationParameters.chosenDeclarationFields));
            put("dec_single_fields", getFieldsString(ApplicationParameters.chosenDeclarationSingleFields));
            put("rep_dec_table_fields", getFieldsString(ApplicationParameters.chosenDeclarationReportsFields));
            put("rep_dec_single_fields", getFieldsString(ApplicationParameters.chosenDeclarationReportsSingleFields));
            if (bookmarkID > 0) {
                put("bookmark_id", Integer.toString(bookmarkID));
            }
        }}, getApplicationContext());
        Log.e("response", obj.toString());
        return obj;

    }

    public String getFieldsString(ArrayList<TripleTableField> fields) {
        String cs = "";
        if (fields.size() > 0) cs = fields.get(0).getName();
        for (int i = 1; i < fields.size(); i++) {
            cs += "," + fields.get(i).getName();
        }
        return cs;
    }

    protected class FieldsRequest extends AsyncTask<Pair<String, HashMap<String, String>>, Void, JSONObject> {
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
                    Toast.makeText(SettingsActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
                } else
                if (res.has("error")) {
                    if (res.getInt("status_code") == 401) {
                        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                        intent.putExtra("login", true);
                        startActivity(intent);
                        finish();
                    } else {
                        showProgress(false);
                        Toast.makeText(getApplicationContext(), res.getString("error"), Toast.LENGTH_LONG).show();
                    }
                } else {
                    saveCredentials(res.getString("access_token"), "access_token");
                    if (ApplicationParameters.activity != null) {
                        MainActivity.saveChanges(getApplicationContext().getSharedPreferences("UserPreference", 0));
                    }
                    loadSettings();
                    if (type == SettingsType.TABLE && (ApplicationParameters.activity instanceof SearchResults)) {
                        ApplicationParameters.changed = true;
                        (new SearchRequest()).execute();
                    } else if (type == SettingsType.SINGLE && (ApplicationParameters.activity instanceof ViewPagerSearchActivity)) {
                         ApplicationParameters.changed = true;
                         (new SearchRequest()).execute();
                    } else if (type == SettingsType.DEC_TABLE && (ApplicationParameters.activity instanceof BaseSearchResults)) {
                        ApplicationParameters.changed = true;
                        finish();
                    } else if (type == SettingsType.DEC_SINGLE && (ApplicationParameters.activity instanceof BaseViewPagerSearchActivity)) {
                        ApplicationParameters.changed = true;
                        finish();
                    }
                    else if (type == SettingsType.REP_DEC_TABLE && (ApplicationParameters.activity instanceof BaseReportsSearchResults)) {
                        ApplicationParameters.changed = true;
                        finish();
                    } else if (type == SettingsType.REP_DEC_SINGLE && (ApplicationParameters.activity instanceof BaseReportsViewPagerSearchActivity)) {
                        ApplicationParameters.changed = true;
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


    public class SearchRequest extends AsyncTask<Pair<String, HashMap<String, String>>, Void, JSONObject> {
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
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                intent.putExtra("login", true);
                startActivity(intent);
                finish();
            } else {
                try {
                    if (res == null) {
                        Toast.makeText(SettingsActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
                    } else
                        saveCredentials(res.getString("access_token"), "access_token");
                    showProgress(false);
                    if (type == SettingsType.TABLE) {
                        ((SearchResults) ApplicationParameters.activity).jsonResults = new JSONArray(res.getString("result"));
                        ApplicationParameters.currentLength = Integer.parseInt(res.getString("count"));

                    } else if (type == SettingsType.SINGLE ) {
                        try {
                            ((ViewPagerSearchActivity) ApplicationParameters.activity).refresh(new JSONArray(res.getString("result")));
                            ((ViewPagerSearchActivity) ApplicationParameters.activity).parentActivity.jsonResults = new JSONArray(res.getString("result"));
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        type = SettingsType.values()[getIntent().getIntExtra("adapterType", 1)];
        bookmarkID = getIntent().getIntExtra("bookmarkID", 0);
        switch (type) {
            case PUSH:
                allFields.addAll(ApplicationParameters.tableFields);
                chosenFields.addAll(ApplicationParameters.chosenTableFields);
                saveString = "all";
                break;
            case SINGLE:
                allFields.addAll(ApplicationParameters.tableFields);
                chosenFields.addAll(ApplicationParameters.chosenSearchResultSingleFields);
                saveString = "single_view";
                break;
            case TABLE:
                allFields.addAll(ApplicationParameters.tableFields);
                chosenFields.addAll(ApplicationParameters.chosenSearchResultsTableFields);
                saveString = "table_view";
                break;
            case DEC_SINGLE:
                allFields.addAll(ApplicationParameters.declarationFields);
                chosenFields.addAll(ApplicationParameters.chosenDeclarationSingleFields);
                saveString = "dec_single_view";
                break;
            case DEC_TABLE:
                allFields.addAll(ApplicationParameters.declarationFields);
                chosenFields.addAll(ApplicationParameters.chosenDeclarationFields);
                saveString = "dec_table_view";
                break;
            case REP_DEC_SINGLE:
                allFields.addAll(ApplicationParameters.declarationFields);
                chosenFields.addAll(ApplicationParameters.chosenDeclarationReportsSingleFields);
                saveString = "rep_dec_single_view";
                break;
            case REP_DEC_TABLE:
                allFields.addAll(ApplicationParameters.declarationFields);
                chosenFields.addAll(ApplicationParameters.chosenDeclarationReportsFields);
                saveString = "rep_dec_table_view";
                break;
        }
        setLayoutId(R.layout.activity_test);
        super.onCreate(savedInstanceState);
        lvMain = (ListView)findViewById(R.id.testList);
        adapter = new SettingsChosenListAdapter(SettingsActivity.this, R.layout.text_edit_list_layout_ver2, chosenFields);
        alertAdapter = new FieldsListAdapter(SettingsActivity.this, R.layout.text_edit_list_layout_ver2, allFields);
        alertAdapter.setChosenAdapter(adapter);
        adapter.flAdapter = alertAdapter;

        lvMain.setAdapter(adapter);
        if (lvMain instanceof DragNDropListView) {
            ((DragNDropListView) lvMain).setDropListener(mDropListener);
            ((DragNDropListView) lvMain).setRemoveListener(mRemoveListener);
            ((DragNDropListView) lvMain).setDragListener(mDragListener);
        }
        Button alertButton = (Button)findViewById(R.id.addFields);
        alertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LinearLayout view2 = (LinearLayout) getLayoutInflater()
                        .inflate(R.layout.custom_dialog, null);

                lvMain2 = (ListView)view2.findViewById(R.id.alertFieldsList);
                lvMain2.setAdapter(alertAdapter);
                ExtendedEditText et = (ExtendedEditText) view2.findViewById(R.id.search);
                final CheckBox cb = (CheckBox)view2.findViewById(R.id.selectAll);
                if (alertAdapter.items.size() == adapter.items.size()) {
                    cb.setText("Удалить все");
                    cb.setChecked(true);
                }
                cb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cb.setChecked(!cb.isSelected());
                    }
                });
                cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (b) {
                            cb.setText("Удалить все");
                            adapter.items.clear();
                            adapter.items.addAll(alertAdapter.items);

                        } else {
                            cb.setText("Выбрать все");
                            adapter.items.clear();
                        }
                        alertAdapter.refresh();
                        alertAdapter.notifyDataSetChanged();
                        adapter.notifyDataSetChanged();
                    }
                });
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
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
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
        bt4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkChanges()) {
                    showDialog(1);
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            if (checkChanges()) {
                showDialog(1);
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private DropListener mDropListener =
            new DropListener() {
                public void onDrop(int from, int to) {
                    if (adapter instanceof SettingsChosenListAdapter) {
                        ((SettingsChosenListAdapter)adapter).onDrop(from, to);
                        lvMain.invalidateViews();
                        adapter.notifyDataSetChanged();

                    }
                }
            };

    private RemoveListener mRemoveListener =
            new RemoveListener() {
                public void onRemove(int which) {
                    if (adapter instanceof SettingsChosenListAdapter) {
                        ((SettingsChosenListAdapter)adapter).onRemove(which);
                        lvMain.invalidateViews();
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
