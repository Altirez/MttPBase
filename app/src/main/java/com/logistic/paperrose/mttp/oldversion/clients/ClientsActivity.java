package com.logistic.paperrose.mttp.oldversion.clients;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.logistic.paperrose.mttp.oldversion.LoginActivity;
import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;
import com.logistic.paperrose.mttp.oldversion.utils.ConnectionActivity;
import com.logistic.paperrose.mttp.oldversion.utils.ExecutionHandler;
import com.logistic.paperrose.mttp.oldversion.utils.ExtendedEditText;
import com.logistic.paperrose.mttp.oldversion.utils.RequestWithCheck;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

public class ClientsActivity extends ConnectionActivity implements ExecutionHandler {


    ArrayList<String> clients = new ArrayList<String>();
    ListView clientList;
    public int currentOffset = 0;
    ClientsAdapter adapter;

    public String type;
    RequestWithCheck rq;
    Button prev;
    Button next;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLayoutId(R.layout.activity_clients);
        super.onCreate(savedInstanceState);
        ApplicationParameters.chosenDeclarationType = "all";
        type = getIntent().getStringExtra("adapterType");
        if (type.equals("LIST_CLIENT")) {
            setTitle("Клиенты");
        } else if (type.equals("LIST_CUSTOMER")) {
            setTitle("Партнеры");
        } else if (type.equals("LIST_PARK")) {
            setTitle("Перевозчики");
        } else if (type.equals("LIST_LINE")) {
            setTitle("Линии");
        } else if (type.equals("LIST_TERMINAL")) {
            setTitle("Терминалы");
        } else if (type.equals("LIST_PAYER")) {
            setTitle("Наши фирмы");
        }
        setupActionBarWithText(getTitle().toString());
        refreshRequest();
        /*} else {
            showProgress(true);
            rq.execute(new Pair<String, HashMap<String, String>>("http://"+ApplicationParameters.MAIN_DOMAIN +"/partners", new HashMap<String, String>() {{
                put("access_token" ,access_token);
                put("hs", LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET));
            }}));
        }
*/
        this.clientList = (ListView)findViewById(R.id.clientList);
        ExtendedEditText et = (ExtendedEditText) findViewById(R.id.search);
        et.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        prev = (Button)findViewById(R.id.prev);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentOffset -= 100;
                refreshRequest();
            }
        });
        next = (Button)findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentOffset += 100;
                refreshRequest();
            }
        });
    }

    private void setPrevNext() {
        prev.setVisibility(currentOffset == 0 ? View.GONE : View.VISIBLE);
        next.setVisibility(clients.size() < 100 ? View.GONE : View.VISIBLE);
    }


    @Override
    public void refreshRequest() {
        rq = new RequestWithCheck();
        rq.setActivity(ClientsActivity.this);
        rq.setHandler(ClientsActivity.this);
        final String access_token = getPref("access_token");
        final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);

        showProgress(true);
        rq.execute(new Pair<String, HashMap<String, String>>("http://"+ ApplicationParameters.MAIN_DOMAIN +"/search/dict", new HashMap<String, String>() {{
            put("access_token" ,access_token);
            put("hs", LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET));
            put("table_name", type);
            put("offset", Integer.toString(currentOffset));
        }}));
    }

    @Override
    public void Execute() {
        clients.clear();
        showProgress(false);

        try {
            saveCredentials(ClientsActivity.this.result.getString("access_token"), "access_token");
            ApplicationParameters.currentDictFields.clear();

            JSONArray fields = ClientsActivity.this.result.getJSONArray("fields");
            for (int i = 0; i < fields.length(); i++) {
                ApplicationParameters.currentDictFields.add(new TripleTableField(
                        fields.getJSONObject(i).getString("FIELD_KEY"),
                        fields.getJSONObject(i).getString("FIELD_DESC"),
                        fields.getJSONObject(i).getString("FIELD_TYPE")));
            }
            JSONArray arr = ClientsActivity.this.result.getJSONArray("result");
            ApplicationParameters.tempResults = new JSONArray();
            ApplicationParameters.lastResults = arr;
            for (int i = 0; i < arr.length(); i++) {
                ApplicationParameters.tempResults.put(arr.getJSONObject(i));
                clients.add(arr.getJSONObject(i).getString(type + "_NAME"));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter = new ClientsAdapter(ClientsActivity.this, R.layout.client_view, clients, type + "_NAME");
        this.clientList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        setPrevNext();
    }



}
