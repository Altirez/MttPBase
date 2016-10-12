package com.logistic.paperrose.mttp.oldversion.settings;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.LruCache;
import android.util.Pair;

import com.logistic.paperrose.mttp.oldversion.BaseLogisticActivity;
import com.logistic.paperrose.mttp.oldversion.clients.SettingsType;
import com.logistic.paperrose.mttp.oldversion.menu.ItemNode;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.utils.LogRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by paperrose on 29.12.2014.
 */

//GOD OBJECT 2
public class ApplicationParameters {
    public static final String CLIENT_ID = "5001";
    public static int offset = 0;
    public static boolean changed = false;
    public static boolean simpleText = true;
    public static int group_id = -1;
    public static int resultSizeLimit = 25;
    public static int[] colors = new int[] { 0xffffffff, 0xffededee };
    public static String chosenDeclarationType = "all";
    public static String localPassword = "1234";
    public static JSONArray lastResults = null;
    public static JSONArray tempResults = null;
    public static ArrayList<TripleTableField>  doc_nomenclatures = new ArrayList<TripleTableField>();
    public static HashMap<String, ArrayList<String>> trafficDocuments = new HashMap<String, ArrayList<String>>();
    public static HashMap<String, ArrayList<String>> trafficDocuments2 = new HashMap<String, ArrayList<String>>();
    public static HashMap<String, ArrayList<TripleTableField>> eds = new HashMap<String, ArrayList<TripleTableField>>();
    public static BaseLogisticActivity activity = null;
    public static final String HISTORY_FILENAME = "93.185.177.75";
    public static int currentLength = 0;
    /*89.255.112.196 ТББ
    public static boolean isCanavara = false;
    public static boolean isTBB = true;
    public static final String SERVER_ADDRESS = "185.97.165.7";
    public static final String MAIN_DOMAIN = SERVER_ADDRESS + "/api/public/index.php";
    public static final String docString = "/21f64da1e5792c8295b964d159a14491";*/

    /*"212.68.8.90" канавара*/
    public static boolean isCanavara = true;
    public static boolean isTBB = false;
    public static final String SERVER_ADDRESS = "212.68.8.90";
    public static final String MAIN_DOMAIN = SERVER_ADDRESS + "/api/index.php";
    public static final String docString = "/documents";
    /*smart-voip.ru:8088 транслайн
    public static boolean isCanavara = false;
    public static boolean isTBB = false;
    public static final String SERVER_ADDRESS = "smart-voip.ru:8088";
    public static final String MAIN_DOMAIN = SERVER_ADDRESS + "/web/api/public/index.php";
    public static final String docString = "/documents";
    /**/
    public static boolean cached = false;
    public static JSONObject lastSearchString = null;
    public static ItemNode lastItem = null;
    public static boolean lightScreen = false;
    public static boolean soundNotify = true;
    public static boolean vibrateNotify = true;
    public static int notifyLimit = 25;
    public static int messageLimit = 25;
    public static JSONObject decLastSearchString = null;
    public static final String CLIENT_SECRET = "rQiiy7U2B2isOJ2vspWniqCtv8bDHI1ENmnewt6hpjNUQg8p3sgy4lOESkmoobxw";
    public static final String AUTH_URL = "http://" +ApplicationParameters.MAIN_DOMAIN + "/user/auth";
    public static String GCM_KEY = "";
    public static String PROPERTY_REG_ID;
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static ArrayList<TripleTableField> tableFields = new ArrayList<TripleTableField>();
    public static ArrayList<TripleTableField> dictionaries = new ArrayList<TripleTableField>();
    public static JSONArray current_statuses = null;
    public static JSONArray current_gtd_statuses = null;
    public static JSONArray canavara_gtd_statuses = null;
    public static JSONArray tl_current_statuses = null;
    public static ArrayList<TripleTableField> status_counts = new ArrayList<TripleTableField>();
    public static HashMap<Integer, Drawable> cachedImages2 = new HashMap<Integer, Drawable>();
    public static LruCache<String, Bitmap> cachedImages = null;
    public static ArrayList<TripleTableField> statuses = new ArrayList<TripleTableField>() {
        {
            add(new TripleTableField("0", "Все записи"));
            add(new TripleTableField("1", "Подготовка ДТ"));
            add(new TripleTableField("2", "ДТ в работе"));
            add(new TripleTableField("3", "Доработка ДТ"));
            add(new TripleTableField("4", "ДТ готова"));
            add(new TripleTableField("5", "Согласование с фин.отделом"));
            add(new TripleTableField("6", "Согласование к подаче"));
            add(new TripleTableField("7", "План подач"));

        }
    };

    public static HashMap<SettingsType, String> typeToStr = new HashMap<SettingsType, String>() {
        {
            put(SettingsType.DEC_SINGLE, "dec_single_view");
            put(SettingsType.DEC_TABLE, "dec_table_view");
            put(SettingsType.REP_DEC_SINGLE, "rep_dec_single_view");
            put(SettingsType.REP_DEC_TABLE, "rep_dec_table_view");
            put(SettingsType.DECLARATION, "declarations");
            put(SettingsType.SINGLE, "single_view");
            put(SettingsType.TABLE, "selected");
        }
    };

    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        //Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        //Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        //Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        //Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        //Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }


    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    private static String AES(String text) {
        try {
            byte[] raw = text.getBytes();
            byte[] secret = CLIENT_SECRET.getBytes();
            byte[] keyStart = CLIENT_SECRET.substring(0, 16).getBytes();
            for (int i = 0; i < 16; i++) {
                keyStart[i] = (byte)((secret[i] + secret[i+16] + secret[i+32] + secret[i+48]) % 128);
            }
            return new String(encrypt(keyStart, raw));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean loginOpened = false;
    public static ArrayList<TripleTableField> declarationFields = new ArrayList<TripleTableField>();
    public static ArrayList<TripleTableField> chosenDeclarationFields = new ArrayList<TripleTableField>();
    public static ArrayList<TripleTableField> chosenDeclarationSingleFields = new ArrayList<TripleTableField>();
    public static ArrayList<TripleTableField> chosenDeclarationSearchFields = new ArrayList<TripleTableField>();
    public static int barHeights = 0;

    public static ArrayList<TripleTableField> chosenDeclarationReportsFields = null;// new ArrayList<TripleTableField>();
    public static ArrayList<TripleTableField> chosenDeclarationReportsSingleFields = null;// new ArrayList<TripleTableField>();
    public static ArrayList<TripleTableField> chosenDeclarationReportsSearchFields = null;// new ArrayList<TripleTableField>();
    //public static ArrayList<TripleTableField> chosenReportFields = new ArrayList<TripleTableField>();
    //public static ArrayList<TripleTableField> chosenReportSingleFields = new ArrayList<TripleTableField>();

    public static ArrayList<TripleTableField> declarationDictionaries = new ArrayList<TripleTableField>();
    public static ArrayList<TripleTableField> chosenTableFields = new ArrayList<TripleTableField>();
    public static ArrayList<TripleTableField> chosenSearchFields = new ArrayList<TripleTableField>();
    public static String decType = "all";
    public static ArrayList<TripleTableField> chosenSearchResultsTableFields = new ArrayList<TripleTableField>();
    public static ArrayList<TripleTableField> chosenSearchResultSingleFields = new ArrayList<TripleTableField>();
    public static ArrayList<TripleTableField> currentDictFields = new ArrayList<TripleTableField>();
    public static ArrayList<TableEntity> menuEntities = new ArrayList<TableEntity>();
    public static ArrayList<Report> reports = new ArrayList<Report>();
    public static ArrayList<Bookmark> bookmarks = new ArrayList<Bookmark>();
    public static HashMap<Integer, Pair<Integer, String>> customBookmarks = new HashMap<Integer, Pair<Integer, String>>();
    public static ArrayList<String> history = new ArrayList<String>();
    public static ArrayList<LogRecord> request_history = new ArrayList<LogRecord>();
    public static BroadcastReceiver receiver = null;
    public static void addRequest(String text) {
        if (request_history.size() > 9) {
            request_history.remove(0);
        }
        request_history.add(new LogRecord(text));
    }

    public static String getOrder(ArrayList<TripleTableField> tableList, ArrayList<TripleTableField> singleList) {
        String res = "";
        String p1 = "";
        String p2 = "";
        if (tableList.size() < 1 || singleList.size() < 1) return null;
        p1 += Integer.toString(tableList.get(0).id);
        p2 += Integer.toString(singleList.get(0).id);
        for (int i = 1; i < tableList.size(); i++) {
            p1 += ">" + Integer.toString(tableList.get(i).id);
        }

        for (int i = 1; i < singleList.size(); i++) {
            p2 += ">" + Integer.toString(singleList.get(i).id);
        }
        return p1 + "<<<" + p2;
    }


    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static String getFieldType(String key, ArrayList<TripleTableField> list) {
        for (TripleTableField tf : list) {
            if (tf.getName().equals(key)) {
                return tf.getType();
            }
        }
        return "";
    }

    public static void setTrafficDocuments(JSONArray baseResults) {
        trafficDocuments.clear();
        trafficDocuments2.clear();
        for (int i = 0; i < baseResults.length(); i++) {
            try {

                String docStr = baseResults.getJSONObject(i).getString("DOCUMENTS");
                trafficDocuments.put(baseResults.getJSONObject(i).getString("RECORD_ID"), new ArrayList<String>());
                docStr = docStr.replace('\\', '/');
                String[] docs = docStr.split(",");
                for (int j = 0; j < docs.length; j++) {
                    trafficDocuments.get(baseResults.getJSONObject(i).getString("RECORD_ID")).add(docs[j]);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                String docStr = baseResults.getJSONObject(i).getString("ADD_DOCUMENTS");
                trafficDocuments2.put(baseResults.getJSONObject(i).getString("RECORD_ID"), new ArrayList<String>());
                docStr = docStr.replace('\\', '/');
                String[] docs = docStr.split(",");
                for (int j = 0; j < docs.length; j++) {
                    trafficDocuments2.get(baseResults.getJSONObject(i).getString("RECORD_ID")).add(docs[j]);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setEDDocuments(JSONArray baseResults) {
        eds.clear();
        for (int i = 0; i < baseResults.length(); i++) {
            try {

                JSONArray docs = baseResults.getJSONObject(i).getJSONArray("records");
                eds.put(baseResults.getJSONObject(i).getString("dec_id"), new ArrayList<TripleTableField>());
                for (int j = 0; j < docs.length(); j++) {
                    JSONObject obj0 = docs.getJSONObject(j);
                    eds.get(baseResults.getJSONObject(i).getString("dec_id")).add(new TripleTableField(obj0.getString("date_ed"), obj0.getString("text_ed")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void convertToTriple(ArrayList<TableField> fields, ArrayList<TripleTableField> locals) {
        locals = new ArrayList<TripleTableField>();
        for (int i = 0; i < fields.size(); i++) {
            locals.add(new TripleTableField(fields.get(i).getName(), fields.get(i).getText()));
        }
    }

    public static void addChosenField(TripleTableField field, ArrayList<TripleTableField> list) {
        for (int i = 0; i < list.size(); i++) {
            if (field.getName() == list.get(i).getName()) return;
        }
        list.add(field);
    }

    public static void setBookmarkOrderByID(int id, String order) {
        for (Bookmark tf : ApplicationParameters.bookmarks) {
            if (tf.getId() == id) {
                tf.order = order;
            }
        }
    }

    public static void setBookmarkDescByID(int id, String order) {
        for (Bookmark tf : ApplicationParameters.bookmarks) {
            if (tf.getId() == id) {
                tf.setDescription(order);
            }
        }
    }

    public static void removeBookmarkByID(int id) {
        Bookmark res = null;
        for (Bookmark tf : ApplicationParameters.bookmarks) {
            if (tf.getId() == id) {
                res = tf;
            }
        }
        if (res != null) {
            ApplicationParameters.bookmarks.remove(res);
        }
    }

    public static Report getReportByID(int id) {
        for (Report tf : ApplicationParameters.reports) {
            if (tf.getId() == id) {
                return tf;
            }
        }
        return null;
    }

    public static String getBookmarkOrderByID(int id) {
        for (Bookmark tf : ApplicationParameters.bookmarks) {
            if (tf.getId() == id) {
                return tf.order;
            }
        }
        return "";
    }
    public static String getCustomBookmarkOrderByID(int id) {
        Pair<Integer, String> cb = customBookmarks.get(id);
        if (cb != null) return cb.second;
        return "";
    }


    public static String getBookmarkDescByID(int id) {
        for (Bookmark tf : ApplicationParameters.bookmarks) {
            if (tf.getId() == id) {
                return tf.getDescription();
            }
        }
        return "";
    }

    public static String getBookmarkNameByID(int id) {
        for (Bookmark tf : ApplicationParameters.bookmarks) {
            if (tf.getId() == id) {
                return tf.getName();
            }
        }
        return "";
    }

    public static String getTableFieldKeyByName(String name, ArrayList<TripleTableField> list) {
        for (TripleTableField tf : list) {
            if (tf.getText().equals(name)) {
                return tf.getName();
            }
        }
        return "";
    }


    public static TripleTableField getTableFieldByKey(String key, ArrayList<TripleTableField> list) {
        for (TripleTableField tf : list) {
            if (tf.getName().equals(key)) {
                return tf;
            }
        }
        return null;
    }


    public static TripleTableField getTableFieldById(int id, ArrayList<TripleTableField> list) {
        for (TripleTableField tf : list) {
            if (tf.id == id) {
                return tf;
            }
        }
        return null;
    }


    public static String getTableFieldNameByKey(String key, ArrayList<TripleTableField> list) {
        for (TripleTableField tf : list) {
            if (tf.getName().equals(key)) {
                return tf.getText();
            }
        }
        return "";
    }

    public static TripleTableField getTableFieldNameByID(String id, ArrayList<TripleTableField> list) {
        int id_int = Integer.parseInt(id);
        for (TripleTableField tf : list) {
            if (tf.id == id_int) {
                return tf;
            }
        }
        return null;
    }

    public static void tableFieldsFromJSONArray(JSONArray array) {

        ApplicationParameters.tableFields.clear();
        ApplicationParameters.dictionaries.clear();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject obj = array.getJSONObject(i);
                ApplicationParameters.tableFields.add(new TripleTableField(obj.getString("NAME"),obj.getString("TITLE"),obj.getString("TYPE"), Integer.parseInt(obj.getString("ID"))));
                if (obj.getString("TYPE").equals("dict")) {
                    ApplicationParameters.dictionaries.add(new TripleTableField(obj.getString("NAME"),obj.getString("REAL_TABLE_NAME"),obj.getString("FIELD_TABLE"),obj.getString("DICT_ID_NAME"), Integer.parseInt(obj.getString("ID")) ));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public static void addToHistory(String str) {
        if (history.size() == 10) {
            history.remove(0);
            history.add(str);
        }
    }

    public static void declarationFieldsFromJSONArray(JSONArray array) {

        ApplicationParameters.declarationFields.clear();
        ApplicationParameters.declarationDictionaries.clear();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject obj = array.getJSONObject(i);
                ApplicationParameters.declarationFields.add(new TripleTableField(obj.getString("NAME"),obj.getString("TITLE"),obj.getString("TYPE"),Integer.parseInt(obj.getString("ID"))));
                if (obj.getString("TYPE").equals("dict")) {
                    ApplicationParameters.declarationDictionaries.add(new TripleTableField(obj.getString("NAME"),obj.getString("REAL_TABLE_NAME"),obj.getString("FIELD_TABLE"),obj.getString("DICT_ID_NAME"),Integer.parseInt(obj.getString("ID")) ));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
     /*   if (isCanavara) {
            chosenDeclarationReportsFields.clear();
            chosenDeclarationReportsSingleFields.clear();
            chosenDeclarationReportsFields.add(getTableFieldByKey("DEC_DATE_CLEAR", declarationFields));
            chosenDeclarationReportsFields.add(getTableFieldByKey("DEC_GTD_NUMBER", declarationFields));
            chosenDeclarationReportsFields.add(getTableFieldByKey("DEC_LIST_CLIENT_NAME", declarationFields));
            chosenDeclarationReportsFields.add(getTableFieldByKey("DEC_COUNT_CONT", declarationFields));

            chosenDeclarationReportsSingleFields.add(getTableFieldByKey("DEC_GTD_NUMBER", declarationFields));
            chosenDeclarationReportsSingleFields.add(getTableFieldByKey("DEC_DATE_CLEAR", declarationFields));
            chosenDeclarationReportsSingleFields.add(getTableFieldByKey("DEC_LIST_CLIENT_NAME", declarationFields));
            chosenDeclarationReportsSingleFields.add(getTableFieldByKey("DEC_TOTAL_FEACC_NO", declarationFields));
            chosenDeclarationReportsSingleFields.add(getTableFieldByKey("DEC_LIST_INSPECTOR_OUT_NAME", declarationFields));
            chosenDeclarationReportsSingleFields.add(getTableFieldByKey("DEC_LIST_INSPECTOR_CUS_VET_NAME", declarationFields));
            chosenDeclarationReportsSingleFields.add(getTableFieldByKey("DEC_LIST_INSPECTOR_CUS_VET_NAME2", declarationFields));
            chosenDeclarationReportsSingleFields.add(getTableFieldByKey("DEC_CONT_NUMBERS", declarationFields));
            chosenDeclarationReportsSingleFields.add(getTableFieldByKey("DEC_COUNT_CONT", declarationFields));
            chosenDeclarationReportsSingleFields.add(getTableFieldByKey("DEC_IS_TS", declarationFields));
            chosenDeclarationReportsSingleFields.add(getTableFieldByKey("DEC_FEACC_TEXT", declarationFields));
            chosenDeclarationReportsSingleFields.add(getTableFieldByKey("DEC_NOTE", declarationFields));
            chosenDeclarationReportsSingleFields.add(getTableFieldByKey("DEC_SUM_VALUE1", declarationFields));
            chosenDeclarationReportsSingleFields.add(getTableFieldByKey("DEC_SUM_VALUE2", declarationFields));
            chosenDeclarationReportsSingleFields.add(getTableFieldByKey("DEC_SUM_VALUE3", declarationFields));
            chosenDeclarationReportsSingleFields.add(getTableFieldByKey("DEC_SUM_VALUE4", declarationFields));
        }*/
    }

    public static void testTableFields() {
        ApplicationParameters.tableFields.clear();
     /*   ApplicationParameters.tableFields.add(new TripleTableField("field1", "Первое поле"));
        ApplicationParameters.tableFields.add(new TripleTableField("field2", "Одно поле"));
        ApplicationParameters.tableFields.add(new TripleTableField("field3", "Другое поле"));
        ApplicationParameters.tableFields.add(new TripleTableField("field4", "Третье поле"));
        ApplicationParameters.tableFields.add(new TripleTableField("field5", "Что-то"));
        ApplicationParameters.tableFields.add(new TripleTableField("field6", "Тест"));
        ApplicationParameters.tableFields.add(new TripleTableField("field7", "Другой тест"));
        ApplicationParameters.tableFields.add(new TripleTableField("field8", "Привет, мир"));
        ApplicationParameters.tableFields.add(new TripleTableField("field9", "Пока, мир"));
        ApplicationParameters.tableFields.add(new TripleTableField("field10", "Антон"));
        ApplicationParameters.tableFields.add(new TripleTableField("field11", "Неантон"));*/
    }

    public static JSONObject getHistory() {

        BufferedReader reader = null;
        JSONObject jObj = null;
        try {
            reader = new BufferedReader(new FileReader(HISTORY_FILENAME), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            String json = sb.toString();
            json = new String(new String(json.getBytes("ISO-8859-1"), "windows-1251").getBytes(), "UTF-8");
            jObj = new JSONObject(json);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jObj;
    }


    public static void setHistory(JSONObject history, Context context) {

        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(context.openFileOutput(HISTORY_FILENAME, Context.MODE_PRIVATE)));
            bw.write(history.toString());
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
