package com.logistic.paperrose.mttp.oldversion.utils;

/**
 * Created by paperrose on 17.12.2014.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.os.NetworkOnMainThreadException;
import android.util.Log;

import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;

public class JSONParser {

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";

    // constructor
    public JSONParser() {

    }

    public void saveRequests(Context context) {
        String history = "";
        SharedPreferences mPrefs = context.getSharedPreferences("UserPreference", 0);
        SharedPreferences.Editor editor = mPrefs.edit();
        for (int i = 0; i < ApplicationParameters.request_history.size(); i++) {
            editor.putString("req" + Integer.toString(i + 1), ApplicationParameters.request_history.get(i).toString());
        }
        editor.commit();
    }

    public JSONObject getJSONFromUrl(String url, HashMap<String, String> vars, Context context) {
        try {
            is = null;
            jObj = null;
            json = "";
            int timeoutConnection = 60000;
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 57000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
            HttpPost httpPost = new HttpPost(url);
            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            ArrayList<String> keys = new ArrayList<String>(vars.keySet());
            for (String key : keys) {
                params.add(new BasicNameValuePair(key, vars.get(key)));
            }

            String s="";
            s += " OS Version: " + System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
            s += "\n OS API Level: "+android.os.Build.VERSION.RELEASE + "("+android.os.Build.VERSION.SDK_INT+")";
            s += "\n Device: " + android.os.Build.DEVICE;
            s += "\n Model (and Product): " + android.os.Build.MODEL + " ("+ android.os.Build.PRODUCT + ")";
            params.add(new BasicNameValuePair("device_info", s));
            String login = context.getSharedPreferences("UserPreference", 0).getString("login", "");
            if (!login.equals("")) params.add(new BasicNameValuePair("login", login));
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Log.e("url", url + "?" + EntityUtils.toString(httpPost.getEntity()));
            //Log.e("params", params.toString());
            if (!url.contains("error/new")) {
                ApplicationParameters.addRequest(url + "?" + EntityUtils.toString(httpPost.getEntity()));
                saveRequests(context);
            }
            boolean isTimeoutFault = false;
            HttpResponse httpResponse = null;
            int j = 0;
            while (j < 5) {
                try {

                    jObj = null;
                    json = "";


                    System.gc();
                    httpResponse = httpClient.execute(httpPost);
                    HttpEntity httpEntity = httpResponse.getEntity();
                    jObj = new JSONObject();
                    jObj.put("status_code", httpResponse.getStatusLine().getStatusCode());
                    is = httpEntity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            is, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    int value=0;
                    String line2 = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    System.gc();
                    json = sb.toString();
                    json = new String(new String(json.getBytes("ISO-8859-1"), "windows-1251").getBytes(), "UTF-8");
                    jObj = new JSONObject(json);
                    jObj.put("status_code", httpResponse.getStatusLine().getStatusCode());
                    Log.e("json string", jObj.toString());
                    break;
                } catch (ConnectTimeoutException d) {
                    d.printStackTrace();
                } catch (SocketTimeoutException d) {
                    d.printStackTrace();
                } catch (OutOfMemoryError d) {
                    d.printStackTrace();
                } catch (NetworkOnMainThreadException d) {
                    d.printStackTrace();
                }
                j++;
            }

        } catch (HttpHostConnectException d) {
            d.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
        // return JSON String
        return jObj;

    }
}