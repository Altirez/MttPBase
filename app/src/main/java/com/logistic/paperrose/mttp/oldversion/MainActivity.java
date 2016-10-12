package com.logistic.paperrose.mttp.oldversion;

import android.accounts.Account;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.LruCache;
import android.util.Pair;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.mainscreen.MainScreenViewAdapter;
import com.logistic.paperrose.mttp.oldversion.menu.ItemNode;
import com.logistic.paperrose.mttp.oldversion.pushes.MyActivity;
import com.logistic.paperrose.mttp.oldversion.results.SearchResults;
import com.logistic.paperrose.mttp.oldversion.search.DelayedFilteringField;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;
import com.logistic.paperrose.mttp.oldversion.settings.Bookmark;
import com.logistic.paperrose.mttp.oldversion.settings.Report;
import com.logistic.paperrose.mttp.oldversion.settings.TableEntity;
import com.logistic.paperrose.mttp.oldversion.utils.ConnectionActivity;
import com.logistic.paperrose.mttp.oldversion.utils.ExecutionHandler;
import com.logistic.paperrose.mttp.oldversion.utils.LogRecord;
import com.logistic.paperrose.mttp.oldversion.utils.RequestWithCheck;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends ConnectionActivity implements ExecutionHandler {
    GoogleCloudMessaging gcm;
    public static final String EXTRA_MESSAGE = "message";
    String SENDER_ID = "665149531559"; //google application key
    static final String TAG = "GCM Demo";
    String regid;
    public ItemNode currentNode = null;
    Context context;

    ArrayList<ItemNode> chooseNodes(ArrayList<ItemNode> started) {
        ArrayList<ItemNode> res = new ArrayList<ItemNode>();
        for (int i = 0; i < started.size(); i++) {
            if (!started.get(i).onlySide) res.add(started.get(i));
        }
        return res;
    }



    @Override
    public void onBackPressed() {
        if (currentNode != null) {
            if (currentNode.parent != null) {

                setAdapter(new MainScreenViewAdapter(MainActivity.this, chooseNodes(currentNode.parent.getChildNodes())));
                setupActionBarWithText(currentNode.parent.description);
                currentNode = currentNode.parent;
                ApplicationParameters.lastItem = currentNode;
            } else {
                setRootNodes();
                ApplicationParameters.lastItem = null;
                setupActionBar();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ApplicationParameters.lastItem == null)
            setRootNodes();
        else {
            setAdapter(new MainScreenViewAdapter(MainActivity.this, chooseNodes(ApplicationParameters.lastItem.getChildNodes())));
        }
    }

    public void setAdapter(MainScreenViewAdapter adapter) {
      //  GridView grid=(GridView)findViewById(R.id.buttons_grid);
        ListView grid=(ListView)findViewById(R.id.buttons_grid);
        grid.setAdapter(adapter);
       /* AnimationSet set = new AnimationSet(true);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(400);
        set.addAnimation(animation);

        animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
        );
        animation.setDuration(400);
        set.addAnimation(animation);

        LayoutAnimationController controller =
                new LayoutAnimationController(set, 0.25f);
        grid.setLayoutAnimation(controller);*/
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ApplicationParameters.loginOpened = false;
        ApplicationParameters.PROPERTY_REG_ID = getUniqueDeviceID();
        setLayoutId(R.layout.activity_main);
        setFinishAfterClick(false);
        super.onCreate(savedInstanceState);
        if (ApplicationParameters.cachedImages == null) {
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            // Используем 1/8 доступной памяти для кэша.
            final int cacheSize = maxMemory / 8;

            ApplicationParameters.cachedImages = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // Размер кэша измеряем в килобайтах
                    return bitmap.getByteCount() / 1024;
                }
            };
        }
        String history = "";

        ApplicationParameters.chosenDeclarationType = "all";

        loadSettings();
        loadLastRequests();
        setRootNodes();
        (new AppLoadTask()).execute("");
        if (ApplicationParameters.trafficDocuments != null)
            ApplicationParameters.trafficDocuments.clear();
        if (ApplicationParameters.eds != null)
            ApplicationParameters.eds.clear();
        ApplicationParameters.tempResults = null;
        ApplicationParameters.lastResults = null;
        System.gc();
        try {
            final String a_t = getPref("access_token");
            RequestWithCheck rq = new RequestWithCheck();
            rq.setActivity(MainActivity.this);

            showProgress(true);
            if (!ApplicationParameters.cached) {
                ApplicationParameters.tableFields.clear();
                rq.setHandler(MainActivity.this);
                rq.execute(new Pair<String, HashMap<String, String>>("http://" + ApplicationParameters.MAIN_DOMAIN + "/user/available_fields", new HashMap<String, String>() {{
                    put("access_token", a_t);
                    put("hs", LoginActivity.encryptStringSHA512(a_t + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET));
                }}));
            } else {
                rq.setHandler(null);
                rq.execute(new Pair<String, HashMap<String, String>>("http://" + ApplicationParameters.MAIN_DOMAIN + "/user/check_auth", new HashMap<String, String>() {{
                    put("access_token", a_t);
                    put("hs", LoginActivity.encryptStringSHA512(a_t + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET));
                }}));
            }
        } catch (Exception e) {

        }

    }



    private Menu menu;


    public void setRootNodes() {
        ArrayList<ItemNode> tmp_nodes = new ArrayList<ItemNode>();
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).parent == null) {
                tmp_nodes.add(nodes.get(i));
            }
        }
        setAdapter(new MainScreenViewAdapter(MainActivity.this, chooseNodes(tmp_nodes)));
        currentNode = null;
    }

    @Override
    protected void setReceiver() {
        if (checkPlayServices()) {

            context = getApplicationContext();
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);
            ApplicationParameters.GCM_KEY = regid;

            if (regid.isEmpty() || getSharedPreferences("UserPreference", 0).getString("access_token", "") == "") {
                registerInBackground();
                //ApplicationParameters.GCM_KEY = regid;
            } else {
                saveCredentials(regid, "gcm_key");
                LocalBroadcastManager.getInstance(this).registerReceiver(
                        messageReceiver, new IntentFilter("refresh_push_count"));
                try {
                    menu.getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_launcher));
                } catch (Exception e) {
                    isEx = true;
                }

            }
        } else {
            Toast.makeText(getApplicationContext(), "Хьюстон, у нас проблемы", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isEx = false;

    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public void loadLastRequests() {
        SharedPreferences historyPrefs = getSharedPreferences("UserPreference", 0);

        String c;
        int i = 1;
        ApplicationParameters.request_history.clear();
        while (!(c = historyPrefs.getString("req" + Integer.toString(i), "")).isEmpty()) {
            ApplicationParameters.request_history.add(new LogRecord(c, true));
            i += 1;
        }
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    gcm = GoogleCloudMessaging.getInstance(context);
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    ApplicationParameters.GCM_KEY = regid;
                    saveCredentials(regid, "gcm_key");
                    storeRegistrationId(context, regid);
                    if (!ApplicationParameters.loginOpened) {
                        ApplicationParameters.loginOpened = true;
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.putExtra("login", true);
                        startActivity(intent);
                        finish();
                    }
                } catch (Exception ex) {
                    msg = "Error :" + ex.getMessage();
                    Log.e("err", msg);
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
                        messageReceiver, new IntentFilter("refresh_push_count"));

            }
        }.execute(null, null, null);
    }


    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ApplicationParameters.PROPERTY_REG_ID, regId);
        editor.putInt(ApplicationParameters.PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    //public String PROPERTY_REG_ID; //android unique id
    //private static final String PROPERTY_APP_VERSION = "appVersion";

    private String getUniqueDeviceID() {
        TelephonyManager TelephonyMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        String m_szImei = TelephonyMgr.getDeviceId();
        String m_szDevIDShort = "35" + //we make this look like a valid IMEI
                Build.BOARD.length()%10+ Build.BRAND.length()%10 +
                Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 +
                Build.DISPLAY.length()%10 + Build.HOST.length()%10 +
                Build.ID.length()%10 + Build.MANUFACTURER.length()%10 +
                Build.MODEL.length()%10 + Build.PRODUCT.length()%10 +
                Build.TAGS.length()%10 + Build.TYPE.length()%10 +
                Build.USER.length()%10 ; //13 digits
        String m_szAndroidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        String m_szWLANMAC = "";// wm.getConnectionInfo().getMacAddress();
        BluetoothAdapter m_BluetoothAdapter	= null; // Local Bluetooth adapter
        m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String m_szBTMAC = "";// m_BluetoothAdapter.getAddress();

        String m_szLongID = m_szImei + m_szDevIDShort + m_szAndroidID+ m_szWLANMAC + m_szBTMAC;
        // compute md5

        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        m.update(m_szLongID.getBytes(),0,m_szLongID.length());
        // get md5 bytes
        byte p_md5Data[] = m.digest();
        // create a hex string
        String m_szUniqueID = new String();
        for (int i=0;i<p_md5Data.length;i++) {
            int b =  (0xFF & p_md5Data[i]);
            // if it is a single digit, make sure it have 0 in front (proper padding)
            if (b <= 0xF) m_szUniqueID+="0";
            // add number to string
            m_szUniqueID+=Integer.toHexString(b);
        }
        // hex string to uppercase
        m_szUniqueID = m_szUniqueID.toUpperCase();

        Class<?> c = null;
        String serial = "";
        String serial2 = "";
        try {
            c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
            serial2 = android.os.Build.SERIAL;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        Account aaccount[];
        //aaccount = AccountManager.get(getApplicationContext()).getAccountsByType("com.google");
        //int l = aaccount.length;
        String[] arrayOfString = { "android_id" };
        Cursor localCursor = getContentResolver().query(Uri.parse("content://com.google.android.gsf.gservices"), null, null, arrayOfString, null);
        if ((localCursor.moveToFirst()) && (localCursor.getColumnCount() >= 2))
        {
            String str2 = Long.toHexString(Long.parseLong(localCursor.getString(1))).toUpperCase();
            localCursor.close();
            return str2;
        }
        localCursor.close();
        return m_szAndroidID.toUpperCase();
    }


    private SharedPreferences getGcmPreferences(Context context) {
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
        String registrationId = prefs.getString(ApplicationParameters.PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            return "";
        }
        int registeredVersion = prefs.getInt(ApplicationParameters.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = MyActivity.getAppVersion(context);
        if (registeredVersion != currentVersion) {
            return "";
        }
        return registrationId;
    }



    @Override
    public String getJSONParameters() {
        String mainF = ((DelayedFilteringField)findViewById(R.id.simpleSearchField)).getText().toString();
        JSONObject res = new JSONObject();
        try {
            res.put("main_filter", mainF);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return res.toString();
    }

    public void performSearch() {
        final String a_t = getPref("access_token");
        RequestWithCheck rq = new RequestWithCheck();
        rq.setActivity(MainActivity.this);
        rq.setHandler(new SearchExecutor());
        rq.execute(new Pair<String, HashMap<String, String>>("http://"+ApplicationParameters.MAIN_DOMAIN +"/search", new HashMap<String, String>() {{
            put("access_token" ,a_t);
            put("hs", LoginActivity.encryptStringSHA512(a_t + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET));
            put("filter_object", getJSONParameters());
        }}));

    }

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }


    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                // положительная кнопка
                case Dialog.BUTTON_POSITIVE:
                    Intent intent = new Intent(MainActivity.this, SearchResults.class);
                    try {
                        JSONArray arr2 = new JSONArray();
                        JSONArray arr = new JSONArray(result.getString("result"));
                        for (int i = 0; i < 100; i++) {
                            arr2.put(arr.getJSONObject(i));
                        }
                        intent.putExtra("json_results", arr.toString());
                    } catch (JSONException e) {
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

    private void loadTableEntities(JSONArray array) throws JSONException {
        ApplicationParameters.menuEntities.clear();
        for (int i = 0; i < array.length(); i++) {
                TableEntity entity = new TableEntity(Integer.parseInt(array.getJSONObject(i).getString("table_id")), array.getJSONObject(i).getString("table_name"));
                loadTableEntities(array.getJSONObject(i).getJSONArray("subtypes"), entity);
                ApplicationParameters.menuEntities.add(entity);

        }
    }

    private void loadTableEntities(JSONArray array, TableEntity entity) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            if (array.getJSONObject(i).getString("subtype_id").isEmpty()) continue;
                TableEntity entity2 = new TableEntity(Integer.parseInt(array.getJSONObject(i).getString("subtype_id")), array.getJSONObject(i).getString("subtype_name"));
                loadTableEntities(array.getJSONObject(i).getJSONArray("subtypes"), entity2);
                entity.addEntity(entity2);
        }

    }

    private class AvailableTablesExecutor implements ExecutionHandler {
        @Override
        public void Execute() {
        }
    }

    private class SearchExecutor implements ExecutionHandler {
        @Override
        public void Execute() {

            try {
                JSONArray arr = new JSONArray(MainActivity.this.result.getString("result"));
                saveCredentials(MainActivity.this.result.getString("access_token"), "access_token");
                ApplicationParameters.currentLength = Integer.parseInt(MainActivity.this.result.getString("count"));
                if (arr.length() > 100) {
                    showDialog(1);
                } else {
                    Intent intent = new Intent(MainActivity.this, SearchResults.class);
                    intent.putExtra("json_results", MainActivity.this.result.getString("result"));

                    ApplicationParameters.setTrafficDocuments(new JSONArray(MainActivity.this.result.getString("documents")));
                    startActivity(intent);
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
            adb.setMessage("По данным критериям поиска найдено много записей. Будут выведены первые 100. Нажмите \"Отмена\", если хотите уточнить поиск.");
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
        return super.onCreateDialog(id);
    }


    private class AppLoadTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            return "";
        }

        protected void onPostExecute(String result) {
            if (ApplicationParameters.barHeights <= 1) {
                Rect rectgle = new Rect();
                Window window = getWindow();
                window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
                int StatusBarHeight = rectgle.top;
                int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
                int TitleBarHeight = contentViewTop - StatusBarHeight;
                ApplicationParameters.barHeights = StatusBarHeight + TitleBarHeight + ApplicationParameters.dpToPx(48);
            }
        }
    }


    @Override
    public void Execute() {
        try {



            showProgress(false);
            ApplicationParameters.cached = true;
            ApplicationParameters.tableFieldsFromJSONArray(MainActivity.this.result.getJSONArray("fields"));
            ApplicationParameters.declarationFieldsFromJSONArray(MainActivity.this.result.getJSONArray("dec_fields"));
            loadTableEntities(MainActivity.this.result.getJSONArray("tables"));
            JSONArray books = MainActivity.this.result.getJSONArray("bookmarks");
            ApplicationParameters.reports = new ArrayList<Report>();
            if (ApplicationParameters.isCanavara) {
                JSONArray reps = MainActivity.this.result.getJSONArray("reports");
                for (int i = 0; i < reps.length(); i++) {
                    JSONObject obj = reps.getJSONObject(i);
                    ApplicationParameters.reports.add(new Report(Long.parseLong(obj.getString("ID")), obj.getString("NAME"),  obj.getString("GROUP_FIELDS"), obj.getString("FILTER_STRING"), obj.getString("ORDER")));
                }
                JSONArray statuses = MainActivity.this.result.getJSONArray("usual_statuses");
                for (int i = 0; i < statuses.length(); i++) {
                    ApplicationParameters.status_counts.add(new TripleTableField(statuses.getJSONObject(i).getString("ID_STATUS"), statuses.getJSONObject(i).getString("CNT")));
                }

            }
            JSONArray doc_nomenclatures = MainActivity.this.result.getJSONArray("doc_nomenclatures");
            for (int i = 0; i < doc_nomenclatures.length(); i++) {
                ApplicationParameters.doc_nomenclatures.add(new TripleTableField(doc_nomenclatures.getJSONObject(i).getString("ID"), doc_nomenclatures.getJSONObject(i).getString("LIST_DOC_NOMENCLATURE_NAME")));
            }

            ApplicationParameters.current_statuses = null;
            ApplicationParameters.current_gtd_statuses = null;
            try {

                JSONArray gtd_statuses = MainActivity.this.result.getJSONArray("gtd_statuses");
                if (gtd_statuses.length() > 0)
                    if (ApplicationParameters.isCanavara)
                        ApplicationParameters.canavara_gtd_statuses = gtd_statuses;
                    else
                        ApplicationParameters.current_gtd_statuses = gtd_statuses;
                if (!ApplicationParameters.isCanavara) {
                    JSONArray statuses = MainActivity.this.result.getJSONArray("statuses");
                    if (statuses.length() > 0) ApplicationParameters.current_statuses = statuses;
                    JSONArray custom_bookmarks = MainActivity.this.result.getJSONArray("custom_bookmarks");
                    for (int i = 0; i < custom_bookmarks.length(); i++) {
                        JSONObject obj = custom_bookmarks.getJSONObject(i);
                        ApplicationParameters.customBookmarks.put(Integer.parseInt(obj.getString("STATUS_ID")), new Pair<Integer, String>(Integer.parseInt(obj.getString("BOOKMARK_ID")), obj.getString("FIELDS_ORDER")));
                    }

                }
            } catch (Exception e) {
                try {
                    JSONArray tl_statuses = MainActivity.this.result.getJSONArray("tl_statuses");
                    if (tl_statuses.length() > 0) ApplicationParameters.tl_current_statuses = tl_statuses;
                    JSONArray custom_bookmarks = MainActivity.this.result.getJSONArray("custom_bookmarks");
                    for (int i = 0; i < custom_bookmarks.length(); i++) {
                        JSONObject obj = custom_bookmarks.getJSONObject(i);
                        ApplicationParameters.customBookmarks.put(Integer.parseInt(obj.getString("STATUS_ID")), new Pair<Integer, String>(Integer.parseInt(obj.getString("BOOKMARK_ID")), obj.getString("FIELDS_ORDER")));
                    }
                } catch (Exception e2) {
                    JSONArray statuses = MainActivity.this.result.getJSONArray("usual_statuses");
                    for (int i = 0; i < statuses.length(); i++) {
                        ApplicationParameters.status_counts.add(new TripleTableField(statuses.getJSONObject(i).getString("ID_STATUS"), statuses.getJSONObject(i).getString("CNT")));
                    }
                }
            }
            try {
                ApplicationParameters.group_id = Integer.parseInt(MainActivity.this.result.getString("group_id"));
            } catch (Exception e) {
                e.printStackTrace();

            }

            ApplicationParameters.bookmarks.clear();
            for (int i = 0; i < books.length(); i++) {
                Bookmark bm = new Bookmark(Long.parseLong(books.getJSONObject(i).getString("ID")), books.getJSONObject(i).getString("NAME"), books.getJSONObject(i).getString("JSON_DESC"), books.getJSONObject(i).getString("FIELDS_ORDER"));
                try {
                    bm.count = books.getJSONObject(i).getString("RECORDS_COUNT");
                } catch (Exception e) {

                }
                ApplicationParameters.bookmarks.add(bm);
            }
            saveBookmarks();
            setDrawerMenu();
            setRootNodes();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            JSONObject fields = MainActivity.this.result.getJSONObject("orders");
            String f_order = fields.getString("FIELD_ORDER");
            String d_order = fields.getString("DEC_ORDER");
            String rep_d_order = fields.getString("REP_DEC_ORDER");



            String tableOrderStr = "";// f_orders[0];
            String singleOrderStr = "";//f_orders[1];
            String decTableOrderStr = "";//d_orders[0];
            String decSingleOrderStr = "";//d_orders[1];
            String rep_decTableOrderStr = "";//r_d_orders[0];
            String rep_decSingleOrderStr = "";//r_d_orders[1];
            if (!f_order.equals("")) {
                String[] f_orders = f_order.split("<<<", -1);
                tableOrderStr = f_orders[0];
                singleOrderStr = f_orders[1];
            }
            if (!d_order.equals("")) {
                String[] d_orders = d_order.split("<<<", -1);
                decTableOrderStr = d_orders[0];
                decSingleOrderStr = d_orders[1];
            }
            if (!rep_d_order.equals("")) {
                String[] r_d_orders = rep_d_order.split("<<<", -1);
                rep_decTableOrderStr = r_d_orders[0];
                rep_decSingleOrderStr = r_d_orders[1];
            }
            String[] tOrderStr = {};
            if (!tableOrderStr.isEmpty())
                tOrderStr = tableOrderStr.split(">");
            String[] sOrderStr = {};
            if (!singleOrderStr.isEmpty())
                sOrderStr = singleOrderStr.split(">");
            String[] dtOrderStr = {};
            if (!decTableOrderStr.isEmpty())
                dtOrderStr = decTableOrderStr.split(">");
            String[] dsOrderStr = {};
            if (!decSingleOrderStr.isEmpty())
                dsOrderStr = decSingleOrderStr.split(">");
            String[] rep_dtOrderStr = {};
            if (!rep_decTableOrderStr.isEmpty())
                rep_dtOrderStr = rep_decTableOrderStr.split(">");
            String[] rep_dsOrderStr = {};
            if (!rep_decSingleOrderStr.isEmpty())
                rep_dsOrderStr = rep_decSingleOrderStr.split(">");
            ApplicationParameters.chosenSearchResultsTableFields = new ArrayList<TripleTableField>();
            ApplicationParameters.chosenSearchResultSingleFields = new ArrayList<TripleTableField>();
            ApplicationParameters.chosenDeclarationSearchFields = new ArrayList<TripleTableField>();
            ApplicationParameters.chosenDeclarationSingleFields = new ArrayList<TripleTableField>();
            ApplicationParameters.chosenDeclarationReportsSearchFields = new ArrayList<TripleTableField>();
            ApplicationParameters.chosenDeclarationReportsSingleFields = new ArrayList<TripleTableField>();
            for (int i = 0; i < tOrderStr.length; i++) {
                TripleTableField tempField = ApplicationParameters.getTableFieldNameByID(tOrderStr[i], ApplicationParameters.tableFields);
                if (tempField != null)
                    ApplicationParameters.chosenSearchResultsTableFields.add(tempField);
            }

            for (int i = 0; i < sOrderStr.length; i++) {
                TripleTableField tempField = ApplicationParameters.getTableFieldNameByID(sOrderStr[i], ApplicationParameters.tableFields);
                if (tempField != null)
                    ApplicationParameters.chosenSearchResultSingleFields.add(tempField);
            }

            for (int i = 0; i < dtOrderStr.length; i++) {
                TripleTableField tempField = ApplicationParameters.getTableFieldNameByID(dtOrderStr[i], ApplicationParameters.declarationFields);
                if (tempField != null)
                    ApplicationParameters.chosenDeclarationSearchFields.add(tempField);
            }

            for (int i = 0; i < dsOrderStr.length; i++) {
                TripleTableField tempField = ApplicationParameters.getTableFieldNameByID(dsOrderStr[i], ApplicationParameters.declarationFields);
                if (tempField != null)
                    ApplicationParameters.chosenDeclarationSingleFields.add(tempField);
            }

            for (int i = 0; i < rep_dtOrderStr.length; i++) {
                TripleTableField tempField = ApplicationParameters.getTableFieldNameByID(rep_dtOrderStr[i], ApplicationParameters.declarationFields);
                if (tempField != null)
                    ApplicationParameters.chosenDeclarationReportsSearchFields.add(tempField);
            }

            for (int i = 0; i < rep_dsOrderStr.length; i++) {
                TripleTableField tempField = ApplicationParameters.getTableFieldNameByID(rep_dsOrderStr[i], ApplicationParameters.declarationFields);
                if (tempField != null)
                    ApplicationParameters.chosenDeclarationReportsSingleFields.add(tempField);
            }

            saveSettings(ApplicationParameters.chosenSearchResultSingleFields,"single_view");
            saveSettings(ApplicationParameters.chosenSearchResultsTableFields,"table_view");
            saveSettings(ApplicationParameters.chosenDeclarationSearchFields,"dec_single_view");
            saveSettings(ApplicationParameters.chosenDeclarationSingleFields,"dec_table_view");
            saveSettings(ApplicationParameters.chosenDeclarationReportsSearchFields,"rep_dec_single_view");
            saveSettings(ApplicationParameters.chosenDeclarationReportsSingleFields,"rep_dec_table_view");
        } catch (Exception e) {
            e.printStackTrace();
        }


        saveChanges(getSharedPreferences("UserPreference", 0));



    }

    public void saveSettings(ArrayList<TripleTableField> chosenFields, String saveString) {
        SharedPreferences historyPrefs = getSharedPreferences("UserPreference", 0);
        SharedPreferences.Editor prefsEditor = historyPrefs.edit();
        Gson gson = new Gson();
        String json2 = gson.toJson(chosenFields);
        prefsEditor.putString(saveString, json2);
        prefsEditor.commit();
    }

    public String getPref(String key) {
        return getSharedPreferences("UserPreference", 0).getString(key, "123");
    }

    public static void saveCredentials(String hash, String key, SharedPreferences prefs) {
        SharedPreferences mPrefs = prefs;
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(key, hash);
        editor.commit();
    }


    public void logout() {
        GCMRegistrar.unregister(MainActivity.this);
        GCMRegistrar.onDestroy(MainActivity.this);
        try {
            gcm.unregister();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
        SharedPreferences.Editor editor = prefs.edit();
        Intent unregIntent = new Intent("com.google.android.c2dm.intent.UNREGISTER");
        unregIntent.putExtra(SENDER_ID, PendingIntent.getBroadcast(this, 0, new Intent(), 0));
        unregIntent.setPackage("com.google.android");
        unregIntent.setClassName("com.google.android.c2dm.intent", "UNREGISTER");
        startService(unregIntent);
        editor.putString(ApplicationParameters.PROPERTY_REG_ID, "");
        editor.commit();

        SharedPreferences historyPrefs = getSharedPreferences("UserPreference", 0);
        SharedPreferences.Editor prefsEditor = historyPrefs.edit();
        //prefsEditor.putString("login", "SYSDBA");
        prefsEditor.putString("password", "");
        prefsEditor.putString("access_token", "123");
        prefsEditor.commit();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.putExtra("login", true);
        startActivity(intent);
        //finish();
    }

    public static void saveChanges(SharedPreferences prefs) {
        SharedPreferences historyPrefs = prefs;// = getSharedPreferences("UserPreference", 0);
        SharedPreferences.Editor prefsEditor = historyPrefs.edit();
        Gson gson = new Gson();
        String json2 = gson.toJson(ApplicationParameters.chosenTableFields);
        String json1 = gson.toJson(ApplicationParameters.tableFields);
        String json3 = gson.toJson(ApplicationParameters.chosenSearchFields);
        String json4= gson.toJson(ApplicationParameters.dictionaries);

        String json8 = gson.toJson(ApplicationParameters.declarationFields);
        String json9= gson.toJson(ApplicationParameters.declarationDictionaries);
        String json10 = gson.toJson(ApplicationParameters.chosenDeclarationFields);
        String json11= gson.toJson(ApplicationParameters.chosenDeclarationSingleFields);
        String json12 = gson.toJson(ApplicationParameters.chosenDeclarationReportsSingleFields);
        String json13= gson.toJson(ApplicationParameters.chosenDeclarationReportsFields);
        String json5= gson.toJson(ApplicationParameters.chosenSearchResultSingleFields);
        String json6= gson.toJson(ApplicationParameters.chosenSearchResultsTableFields);
        String json7= gson.toJson(ApplicationParameters.bookmarks);
        prefsEditor.putString("all", json1);
        prefsEditor.putString("selected", json2);
        prefsEditor.putString("filters", json3);
        prefsEditor.putString("dictionaries", json4);
        prefsEditor.putString("single_view", json5);
        prefsEditor.putString("table_view", json6);
        prefsEditor.putString("bookmarks", json7);


        prefsEditor.putString("declarations", json8);
        prefsEditor.putString("declaration_dictionaries", json9);
        prefsEditor.putString("dec_single_view", json11);
        prefsEditor.putString("dec_table_view", json10);
        prefsEditor.putString("rep_dec_single_view", json12);
        prefsEditor.putString("rep_dec_table_view", json13);
        prefsEditor.commit();
    }

    private void loadSettings() {
        SharedPreferences historyPrefs = getSharedPreferences("UserPreference", 0);
        Gson gson = new Gson();
        String jsonH = historyPrefs.getString("all", "");
        String jsonS = historyPrefs.getString("selected", "");
        String jsonD = historyPrefs.getString("dictionaries", "");
        String jsonT = historyPrefs.getString("table_view", "");
        String jsonV = historyPrefs.getString("single_view", "");
        String jsonB = historyPrefs.getString("bookmarks", "");

        String jsonDec = historyPrefs.getString("declarations", "");
        String jsonDecD = historyPrefs.getString("declaration_dictionaries", "");
        String jsonDecT = historyPrefs.getString("dec_table_view", "");
        String jsonDecV = historyPrefs.getString("dec_single_view", "");
        String jsonDecRepT = historyPrefs.getString("rep_dec_table_view", "");
        String jsonDecRepV = historyPrefs.getString("rep_dec_single_view", "");
        Type type = new TypeToken<ArrayList<TripleTableField>>(){}.getType();
        Type type2 = new TypeToken<ArrayList<Bookmark>>(){}.getType();
        if (jsonH != "") {
            ApplicationParameters.tableFields = gson.fromJson(jsonH, type);
        }
        final String a_t = getPref("access_token");
        if (ApplicationParameters.tableFields.size() == 0 || ApplicationParameters.declarationFields.size() == 0) {
            RequestWithCheck rq = new RequestWithCheck();
            rq.setActivity(MainActivity.this);
            rq.setHandler(MainActivity.this);
            showProgress(true);
            rq.execute(new Pair<String, HashMap<String, String>>("http://"+ApplicationParameters.MAIN_DOMAIN +"/user/available_fields", new HashMap<String, String>() {{
                put("access_token" ,a_t);
                put("hs", LoginActivity.encryptStringSHA512(a_t + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET));
            }}));
        }
        if (jsonS == "") {
            ApplicationParameters.chosenTableFields = new ArrayList<TripleTableField>();
        } else {
            ApplicationParameters.chosenTableFields = gson.fromJson(jsonS, type);
        }
        if (jsonT == "") {
            ApplicationParameters.chosenSearchResultsTableFields = new ArrayList<TripleTableField>();
        } else {
            ApplicationParameters.chosenSearchResultsTableFields = gson.fromJson(jsonT, type);
        }
        if (jsonV == "") {
            ApplicationParameters.chosenSearchResultSingleFields = new ArrayList<TripleTableField>();
        } else {
            ApplicationParameters.chosenSearchResultSingleFields = gson.fromJson(jsonV, type);
        }

        if (jsonB == "") {
            ApplicationParameters.bookmarks = new ArrayList<Bookmark>();
        } else {
            ApplicationParameters.bookmarks = gson.fromJson(jsonB, type2);
        }

        if (jsonD == "") {
            ApplicationParameters.dictionaries = new ArrayList<TripleTableField>();
        } else {
            ApplicationParameters.dictionaries = gson.fromJson(jsonD, type);
        }

        if (jsonDec == "") {
            ApplicationParameters.declarationFields = new ArrayList<TripleTableField>();
        } else {
            ApplicationParameters.declarationFields = gson.fromJson(jsonDec, type);
        }
        if (jsonDecD == "") {
            ApplicationParameters.declarationDictionaries = new ArrayList<TripleTableField>();
        } else {
            ApplicationParameters.declarationDictionaries = gson.fromJson(jsonDecD, type);
        }

        if (jsonDecT == "") {
            ApplicationParameters.chosenDeclarationFields = new ArrayList<TripleTableField>();
        } else {
            ApplicationParameters.chosenDeclarationFields = gson.fromJson(jsonDecT, type);
        }
        if (jsonDecV == "") {
            ApplicationParameters.chosenDeclarationSingleFields = new ArrayList<TripleTableField>();
        } else {
            ApplicationParameters.chosenDeclarationSingleFields = gson.fromJson(jsonDecV, type);
        }

        if (jsonDecRepT == "") {
            ApplicationParameters.chosenDeclarationReportsFields = new ArrayList<TripleTableField>();
        } else {
            ApplicationParameters.chosenDeclarationReportsFields = gson.fromJson(jsonDecRepT, type);
        }
        if (jsonDecRepV == "") {
            ApplicationParameters.chosenDeclarationReportsSingleFields = new ArrayList<TripleTableField>();
        } else {
            ApplicationParameters.chosenDeclarationReportsSingleFields = gson.fromJson(jsonDecRepV, type);
        }
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pushes.add(new PushItem(intent.getStringExtra("date"), intent.getStringExtra("title"), intent.getStringExtra("description")));
            TextView tv = (TextView)MainActivity.this.findViewById(R.id.counter);
            SharedPreferences unread = getSharedPreferences("UserPreference", 0);
            int unread_count = unread.getInt("unread", 0);
            tv.setText(Integer.toString(unread_count));
            tv.setVisibility(unread_count == 0 ? View.GONE : View.VISIBLE);
        }
    };

    @Override
    public void refreshRequest() {
        ApplicationParameters.tableFields.clear();
        ApplicationParameters.chosenSearchResultsTableFields.clear();
        ApplicationParameters.chosenSearchResultSingleFields.clear();
        ApplicationParameters.declarationFields.clear();
        ApplicationParameters.chosenDeclarationFields.clear();
        ApplicationParameters.chosenDeclarationSingleFields.clear();
        ApplicationParameters.chosenDeclarationReportsFields.clear();
        ApplicationParameters.chosenDeclarationReportsSingleFields.clear();
        final String a_t = getPref("access_token");

        RequestWithCheck rq = new RequestWithCheck();
        rq.setActivity(MainActivity.this);
        showProgress(true);
        if (!ApplicationParameters.cached) {
            rq.setHandler(MainActivity.this);
            rq.execute(new Pair<String, HashMap<String, String>>("http://" + ApplicationParameters.MAIN_DOMAIN + "/user/available_fields", new HashMap<String, String>() {{
                put("access_token", a_t);
                put("hs", LoginActivity.encryptStringSHA512(a_t + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET));

            }}));
        }  else {
            rq.setHandler(null);
            rq.execute(new Pair<String, HashMap<String, String>>("http://" + ApplicationParameters.MAIN_DOMAIN + "/user/check_auth", new HashMap<String, String>() {{
                put("access_token", a_t);
                put("hs", LoginActivity.encryptStringSHA512(a_t + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET));
            }}));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean authorize() {
        if (!isEx) {
            registerInBackground();
        } else {
            isEx = false;
            (new Unregister()).execute();
        }
        return true;
    }


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem mi2 = menu.add(0, 1, 0, "Test login");
        mi2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                registerInBackground();
                return true;
            }
        });

        MenuItem mi3 = menu.add(0, 1, 0, "Refresh");
        mi3.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }*/
}
