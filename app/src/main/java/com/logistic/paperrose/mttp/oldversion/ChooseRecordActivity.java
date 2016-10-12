package com.logistic.paperrose.mttp.oldversion;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.newsearch.AutoCompleteAdapter;
import com.logistic.paperrose.mttp.oldversion.search.DelayedFilteringField;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;
import com.logistic.paperrose.mttp.oldversion.utils.ConnectionActivity;
import com.logistic.paperrose.mttp.oldversion.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by paperrose on 24.04.2015.
 */
public class ChooseRecordActivity extends ConnectionActivity {

    public String id;
    Spinner spinner;
    AutoCompleteTextView myAutoComplete;
    ArrayList<String> myList = new ArrayList<String>();
    ArrayAdapter<String> myAutoCompleteAdapter;
    TextView autoList;
    public String type_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLayoutId(R.layout.activity_choose_record);
        super.onCreate(savedInstanceState);
        myList.clear();
        myAutoComplete = (AutoCompleteTextView) findViewById(R.id.idDocNomenclature);
        for (int i = 0; i < ApplicationParameters.doc_nomenclatures.size(); i++) {
            myList.add(ApplicationParameters.doc_nomenclatures.get(i).getText());
        }
        myAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                type_id = ApplicationParameters.getTableFieldKeyByName(charSequence.toString(), ApplicationParameters.doc_nomenclatures);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        spinner = (Spinner)findViewById(R.id.spinner_type);
        myAutoComplete.setThreshold(1);
        myAutoCompleteAdapter = new ArrayAdapter<String>(ChooseRecordActivity.this,
                android.R.layout.simple_dropdown_item_1line, myList);

        myAutoComplete.setAdapter(myAutoCompleteAdapter);
        if (ApplicationParameters.doc_nomenclatures.isEmpty()) {
            (new GetNomenclaturesTask()).execute();
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        final DelayedFilteringField dff = (DelayedFilteringField) findViewById(R.id.simpleSearchField);
        dff.setThreshold(3);
        dff.enoughToFilterT = false;
        dff.setLoadingIndicator((ProgressBar) findViewById(R.id.progress_bar));
        dff.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                TripleTableField book = (TripleTableField) adapterView.getItemAtPosition(position);
                dff.setText(book.getText());
            }
        });

        dff.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    dff.clearFocus();
                    searchAct();
                    return true;
                }
                return false;
            }
        });

        AutoCompleteAdapter autoCompleteAdapter = new AutoCompleteAdapter(getApplicationContext());
        autoCompleteAdapter.setActivity(ChooseRecordActivity.this);
        dff.setAdapter(autoCompleteAdapter);

    }

    public void searchAct() {

        showProgress(true);
    }


    public class GetNomenclaturesTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... args) {
            JSONObject jsonObject = null;
            try {
                final String access_token = getPref("access_token");
                final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
                JSONParser parser = new JSONParser();
                jsonObject = parser.getJSONFromUrl("http://"+ApplicationParameters.MAIN_DOMAIN +"/get_nomenclatures", new HashMap<String, String>() {{
                    put("access_token", access_token);
                    put("hs", secure_token);
                }}, getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            showProgress(false);
            if (res == null) {
                Toast.makeText(ChooseRecordActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
            } else {
                try {
                    JSONArray doc_nomenclatures = res.getJSONArray("doc_nomenclatures");
                    for (int i = 0; i < doc_nomenclatures.length(); i++) {
                        ApplicationParameters.doc_nomenclatures.add(new TripleTableField(doc_nomenclatures.getJSONObject(i).getString("ID"), doc_nomenclatures.getJSONObject(i).getString("LIST_DOC_NOMENCLATURE_NAME")));
                    }
                    myList.clear();
                    myAutoComplete = (AutoCompleteTextView) findViewById(R.id.idDocNomenclature);
                    for (int i = 0; i < ApplicationParameters.doc_nomenclatures.size(); i++) {
                        myList.add(ApplicationParameters.doc_nomenclatures.get(i).getText());
                    }
                    myAutoCompleteAdapter = new ArrayAdapter<String>(ChooseRecordActivity.this,
                            android.R.layout.simple_dropdown_item_1line, myList);
                    myAutoComplete.setAdapter(myAutoCompleteAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }
    }


    public class GetRecordsTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... args) {
            JSONObject jsonObject = null;
            try {
                final String access_token = getPref("access_token");
                final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
                JSONParser parser = new JSONParser();
                jsonObject = parser.getJSONFromUrl("http://"+ApplicationParameters.MAIN_DOMAIN +"/get_records", new HashMap<String, String>() {{
                    put("access_token", access_token);
                    put("hs", secure_token);
                }}, getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            showProgress(false);
            if (res == null) {
                Toast.makeText(ChooseRecordActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
            } else {

            }

        }
    }
}
