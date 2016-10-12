package com.logistic.paperrose.mttp.oldversion.pushes;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.logistic.paperrose.mttp.oldversion.BaseLogisticActivity;
import com.logistic.paperrose.mttp.oldversion.LoginActivity;
import com.logistic.paperrose.mttp.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.logistic.paperrose.mttp.oldversion.results.BaseSearchResults;
import com.logistic.paperrose.mttp.oldversion.results.SearchResults;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;
import com.logistic.paperrose.mttp.oldversion.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MyActivity extends BaseLogisticActivity {
    ArrayList<PushItem> pushes = new ArrayList<PushItem>();
    TextView mDisplay;
    Context context;
    PushesAdapter adapter = null;
    String currentType = "0";
    public String searchStr = "";
    public int currentOffset = 0;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
       // if (v.getId() == R.id.statusText) {
        //    inflater.inflate(R.menu.push_type_context_menu, menu);
       // } else {
            inflater.inflate(R.menu.pushes_context_menu, menu);
       // }
    }


    public void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", text);
        clipboard.setPrimaryClip(clip);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info;

        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            return false;
        }

        if (info != null && adapter.getItem(info.position).getType().equals("3")) {
            closeContextMenu();
            return false;
        }
        switch (item.getItemId()) {
            case R.id.copy:
                copyToClipboard(adapter.getItem(info.position).getDescription());
                break;
            case R.id.search_rec:
                if (adapter.getItem(info.position).getType().equals("1")) {
                    showProgress(true);
                    (new CheckToken()).execute(adapter.getItem(info.position).record_id);
                } else if (adapter.getItem(info.position).getType().equals("2")) {
                    showProgress(true);
                    (new CheckToken3()).execute(adapter.getItem(info.position).record_id);
                }
                break;
            case R.id.cancel:
                closeContextMenu();
                break;

          /*  case R.id.allItem:
                if (!currentType.equals("0")) {
                    currentType = "0";
                    refreshRequest();
                }
                break;
            case R.id.containersItem:
                if (!currentType.equals("1")) {
                    currentType = "1";
                    refreshRequest();
                }
                break;
            case R.id.declarationsItem:
                if (!currentType.equals("2")) {
                    currentType = "2";
                    refreshRequest();
                }
                break;
            case R.id.messagesItem:
                if (!currentType.equals("3")) {
                    currentType = "3";
                    refreshRequest();
                }
                break;*/
            default: break;
        }
        return true;
    }


    private class PushRefresh extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... args) {
            JSONObject jsonObject = null;
            if (ApplicationParameters.GCM_KEY.isEmpty())
                ApplicationParameters.GCM_KEY = getSharedPreferences("UserPreference", 0).getString("gcm_key", "");
            if (ApplicationParameters.GCM_KEY.isEmpty())
                return new JSONObject() {{
                    Log.e("MyActivity", ApplicationParameters.GCM_KEY);
                    try {
                        put("error", "err");
                        put("status_code", 401);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }};
            try {
                jsonObject = checkServerCredentials2(args[0], args[1], args[2]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            showProgress(false);
            if (res == null) {
                Toast.makeText(MyActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
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
                        Intent intent = new Intent(MyActivity.this, LoginActivity.class);
                        Log.e("MyActivity", "148");
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
                    JSONArray pushes_arr = res.getJSONArray("pushes");
                    pushes.clear();
                    for (int i = pushes_arr.length()-1; i >= 0; i--) {
                        JSONObject push = pushes_arr.getJSONObject(i);
                        String dtcr = push.getString("DT_CREATE");
                        if (dtcr.isEmpty()) {
                            dtcr = new Timestamp((new Date().getTime())).toString();
                        }
                        addItem(new PushItem(Long.toString(Long.parseLong(dtcr) * 1000),push.getString("TITLE"),push.getString("TEXT"),push.getString("TYPE"),push.getString("RECORD_ID")));
                    }
                    adapter.refresh(pushes);
                    adapter.notifyDataSetChanged();
                    preLast = -2;
                    ListView lvMain = (ListView) findViewById(R.id.pushList);
                    lvMain.setSelection(0);
                    setPrevNext();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class CheckToken extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... args) {
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
                jsonObject = checkServerCredentials(args[0], "search");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            showProgress(false);
            if (res == null) {
                Toast.makeText(MyActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
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
                        Intent intent = new Intent(MyActivity.this, LoginActivity.class);
                        Log.e("MyActivity", "221");
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
                Intent intent = new Intent(MyActivity.this, SearchResults.class);
                intent.putExtra("act_type", 1);
                try {
                    ApplicationParameters.lastResults = new JSONArray();
                    ApplicationParameters.tempResults = new JSONArray();
                    JSONArray tmp = new JSONArray(res.getString("result"));
                    ApplicationParameters.currentLength = Integer.parseInt(res.getString("count"));
                    ApplicationParameters.lastResults = tmp;
                    for (int i = 0; i < tmp.length(); i++) {
                        ApplicationParameters.tempResults.put(ApplicationParameters.lastResults.get(i));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {

                    ApplicationParameters.setTrafficDocuments(new JSONArray(res.getString("documents")));
                } catch (Exception e) {

                }
                startActivity(intent);
            }
        }
    }

    private class CheckToken3 extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... args) {
            if (ApplicationParameters.GCM_KEY.isEmpty()) return new JSONObject() {{
                try {
                    put("error", "err");
                    put("status_code", 401);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }};
            JSONObject jsonObject = null;
            String cc = "";
            if (args.length > 1) {
                cc = args[1];
                ApplicationParameters.decType = cc;
            }
            try {

                jsonObject = checkServerCredentials(args[0], "declarations");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            showProgress(false);
            if (res == null) {
                Toast.makeText(MyActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
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
                        Intent intent = new Intent(MyActivity.this, LoginActivity.class);
                        Log.e("MyActivity", "300");
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
                Intent intent = new Intent(MyActivity.this, BaseSearchResults.class);
                try {
                    ApplicationParameters.lastResults = new JSONArray();
                    ApplicationParameters.tempResults = new JSONArray();
                    JSONArray tmp = new JSONArray(res.getString("result"));
                    ApplicationParameters.lastResults = tmp;
                    for (int i = 0; i < tmp.length(); i++) {
                        ApplicationParameters.tempResults.put(ApplicationParameters.lastResults.get(i));
                    }
                    // intent.putExtra("json_results", arr.toString());
                    try {

                        ApplicationParameters.setTrafficDocuments(new JSONArray(res.getString("documents")));
                    } catch (Exception e) {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(intent);

                //return true;
            }
        }
    }

    public ArrayList<PushItem> findMessages() {
        return  new ArrayList<PushItem>();
    }

    public JSONObject checkServerCredentials2(final String offset, final String type, final String search) throws JSONException {
        final String access_token = getPref("access_token");
        final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
        JSONParser parser = new JSONParser();
        JSONObject obj = parser.getJSONFromUrl("http://"+ApplicationParameters.MAIN_DOMAIN +"/user/pushes", new HashMap<String, String>() {{
            put("access_token", access_token);
            put("hs", secure_token);
            put("offset", offset);
            put("type", type);
            put("search", search);
        }}, getApplicationContext());
        return obj;
    }


    public JSONObject checkServerCredentials(final String filter, String type) throws JSONException {
        final String access_token = getPref("access_token");
        final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
        JSONParser parser = new JSONParser();
        JSONObject obj = parser.getJSONFromUrl("http://"+ApplicationParameters.MAIN_DOMAIN +"/"+ type + "/record", new HashMap<String, String>() {{
            put("access_token", access_token);
            put("hs", secure_token);
            put("record_id", filter);
        }}, getApplicationContext());
        return obj;
    }

    String [] types = {"Все", "Грузы", "Декларации", "Сообщения"};

    @Override
    public void refreshRequest() {
        adapter.currentType = currentType;

        setupActionBarWithText(types[Integer.parseInt(currentType)] + " ▼");
        TextView actionBarText = (TextView)findViewById(R.id.statusText);
        actionBarText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentType.equals("0")) {
                    item1.setTitle(Html.fromHtml("<b><font color='#831618'>Все</font></b>"));
                    item2.setTitle(Html.fromHtml("<font color='#35424b'>Грузы</font>"));
                    item3.setTitle(Html.fromHtml("<font color='#35424b'>Декларации</font>"));
                    item4.setTitle(Html.fromHtml("<font color='#35424b'>Сообщения</font>"));
                } else if (currentType.equals("1")) {
                    item2.setTitle(Html.fromHtml("<b><font color='#831618'>Грузы</font></b>"));
                    item1.setTitle(Html.fromHtml("<font color='#35424b'>Все</font>"));
                    item3.setTitle(Html.fromHtml("<font color='#35424b'>Декларации</font>"));
                    item4.setTitle(Html.fromHtml("<font color='#35424b'>Сообщения</font>"));
                } else if (currentType.equals("2")) {
                    item3.setTitle(Html.fromHtml("<b><font color='#831618'>Декларации</font></b>"));
                    item2.setTitle(Html.fromHtml("<font color='#35424b'>Грузы</font>"));
                    item1.setTitle(Html.fromHtml("<font color='#35424b'>Все</font>"));
                    item4.setTitle(Html.fromHtml("<font color='#35424b'>Сообщения</font>"));
                } else if (currentType.equals("3")) {
                    item4.setTitle(Html.fromHtml("<b><font color='#831618'>Сообщения</font></b>"));
                    item2.setTitle(Html.fromHtml("<font color='#35424b'>Грузы</font>"));
                    item3.setTitle(Html.fromHtml("<font color='#35424b'>Декларации</font>"));
                    item1.setTitle(Html.fromHtml("<font color='#35424b'>Все</font>"));
                }
                popupMenu.show();

            }
        });
        showProgress(true);
        (new PushRefresh()).execute(Integer.toString(currentOffset), currentType, searchStr);
    }
    public int preLast = -2;
    MenuItem item1;
    MenuItem item2;
    MenuItem item3;
    MenuItem item4;
    PopupMenu popupMenu;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setLayoutId(R.layout.activity_my);
        super.onCreate(savedInstanceState);
        SharedPreferences unread = getSharedPreferences("UserPreference", 0);
        int unread_count;
        unread_count = 0;
        ApplicationParameters.chosenDeclarationType = "all";
        SharedPreferences.Editor prefsEditor = unread.edit();
        prefsEditor.putInt("unread", unread_count);
        prefsEditor.commit();
        mDisplay = (TextView) findViewById(R.id.display);
        EditText et = (EditText) findViewById(R.id.search);
        et.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    searchStr = s.toString().toUpperCase();
                    refreshRequest();
                } else {
                    if (!searchStr.isEmpty()) {
                        searchStr = "";
                        refreshRequest();
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        context = getApplicationContext();
        try {
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
            mNotificationManager.cancel(1);
        } catch (Exception e) {

        }
        if (ApplicationParameters.receiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("refresh_push_history"));

        adapter = new PushesAdapter(this, pushes);
        ListView lvMain = (ListView) findViewById(R.id.pushList);
        lvMain.setAdapter(adapter);

        lvMain.setOnScrollListener(new AbsListView.OnScrollListener() {
          //  private int preLast;



            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView lw, final int firstVisibleItem,
                                 final int visibleItemCount, final int totalItemCount) {


            }
        });
        refreshRequest();
        loadHistory();
        //if (pushes.size() > 0)
        //lvMain.setSelection(pushes.size()-1);
        registerForContextMenu(lvMain);

        View line = findViewById(R.id.pushMenuLine);
        popupMenu = new PopupMenu(this, line);
        popupMenu.inflate(R.menu.push_type_context_menu);
        item1 = popupMenu.getMenu().findItem(R.id.allItem);
        item2 = popupMenu.getMenu().findItem(R.id.containersItem);
        item3 = popupMenu.getMenu().findItem(R.id.declarationsItem);
        item4 = popupMenu.getMenu().findItem(R.id.messagesItem);
        /**
         * Step 3: Call show() method on the popup menu to display the
         * menu when the button is clicked.
         */

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.allItem:
                        if (!currentType.equals("0")) {
                            currentType = "0";
                            refreshRequest();
                        }
                        break;
                    case R.id.containersItem:
                        if (!currentType.equals("1")) {
                            currentType = "1";
                            refreshRequest();
                        }
                        break;
                    case R.id.declarationsItem:
                        if (!currentType.equals("2")) {
                            currentType = "2";
                            refreshRequest();
                        }
                        break;
                    case R.id.messagesItem:
                        if (!currentType.equals("3")) {
                            currentType = "3";
                            refreshRequest();
                        }
                        break;
                    default: break;
                }
                return false;
            }
        });
        String[] items = new String[]{"Все", "Грузы", "Декларации", "Сообщения"};
        Spinner spinner = (Spinner) findViewById(R.id.type_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                currentType = Integer.toString(position);
                refreshRequest();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void addItem(PushItem push) {
        if (pushes.size() > 19) {
            pushes.remove(19);
        }
        pushes.add(0, push);
        //refreshHistory();
       // adapter.refresh(pushes);
    }

    public void removeItem(int i) {
        pushes.remove(i);
        refreshHistory();
        adapter.refresh(pushes);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            refreshHistory();
        } catch (Exception e) {

        }
    }

    @Override
    public void setParams() {

        super.setParams();
        prev = (Button)findViewById(R.id.prev);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentOffset -= 20;
                refreshRequest();
            }
        });
        next = (Button)findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentOffset += 20;
                refreshRequest();
            }
        });
       // setPrevNext();
    }
    Button prev;
    Button next;

    private void setPrevNext() {
        prev.setVisibility(currentOffset == 0 ? View.GONE : View.VISIBLE);
        next.setVisibility(pushes.size() < 20 ? View.GONE : View.VISIBLE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    public void onClick(final View view) {
        pushes.clear();
        refreshHistory();
        adapter.refresh(pushes);
    }

    public void loadHistory() {
   /*     SharedPreferences historyPrefs = getSharedPreferences("UserPreference", 0);
        Gson gson = new Gson();
        String jsonH = historyPrefs.getString("history", "");
        Type type = new TypeToken<ArrayList<PushItem>>(){}.getType();
        if (jsonH == "")
            pushes = new ArrayList<PushItem>();
        else {
            pushes = gson.fromJson(jsonH, type);
            ArrayList<PushItem> tmp = new ArrayList<PushItem>();
            for (PushItem push : pushes) {
                if (push.getDate() != null) {
                    tmp.add(push);
                }
            }
            pushes = tmp;
        }*/
        try {
            adapter.refresh(pushes);
        } catch (Exception e) {

        }
    }

    public void loadHistory(String type_p) {
        SharedPreferences historyPrefs = getSharedPreferences("UserPreference", 0);
        Gson gson = new Gson();
        String jsonH = historyPrefs.getString("history", "");
        Type type = new TypeToken<ArrayList<PushItem>>(){}.getType();
        if (jsonH == "")
            pushes = new ArrayList<PushItem>();
        else {
            pushes = gson.fromJson(jsonH, type);
            ArrayList<PushItem> tmp = new ArrayList<PushItem>();
            for (PushItem push : pushes) {
                if (push.getDate() != null && push.getType().equals(type_p)) {
                    tmp.add(push);
                }
            }
            pushes = tmp;
        }
        try {
            adapter.refresh(pushes);
            adapter.notifyDataSetChanged();
            findViewById(R.id.counter).setVisibility(View.GONE);
        } catch (Exception e) {

        }
    }

    public void refreshHistory() {
        SharedPreferences historyPrefs = getSharedPreferences("UserPreference", 0);
        SharedPreferences.Editor prefsEditor = historyPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(pushes);
        prefsEditor.putString("history", json);
        prefsEditor.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            findViewById(R.id.buttons).setVisibility(View.GONE);
            findViewById(R.id.real_buttons).setVisibility(View.GONE);
            findViewById(R.id.counter).setVisibility(View.GONE);
            findViewById(R.id.searchButton).setVisibility(View.GONE);
        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            SharedPreferences unread = getSharedPreferences("UserPreference", 0);
            int unread_count = unread.getInt("unread", 0);
            findViewById(R.id.counter).setVisibility(View.GONE);
            findViewById(R.id.buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.real_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.searchButton).setVisibility(View.VISIBLE);
        }
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (currentOffset != 0) return;
            PushItem pi = new PushItem(intent.getStringExtra("date"), intent.getStringExtra("title"), intent.getStringExtra("description"));
            pi.setType(intent.getStringExtra("type"));
            pi.record_id = intent.getStringExtra("record_id");

            if (currentType.equals("0") || currentType.equals(pi.getType())) {
                addItem(pi);
                adapter.notifyDataSetChanged();
            }
           // findViewById(R.id.counter).setVisibility(View.GONE);
        }
    };


}
