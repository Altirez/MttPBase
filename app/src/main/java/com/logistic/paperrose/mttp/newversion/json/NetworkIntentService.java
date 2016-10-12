package com.logistic.paperrose.mttp.newversion.json;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by paperrose on 29.06.2016.
 */
public class NetworkIntentService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */


    public NetworkIntentService() {
        super("Network Intent Service");
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        final JsonResultReceiver receiver = intent.getParcelableExtra(JsonResultReceiver.class.getCanonicalName());
        HttpURLConnection urlConnection = null;
        String response = "";
        receiver.send(Constants.STATUS_STARTED, Bundle.EMPTY);
        final Bundle data = new Bundle();
        try {
            URL url = new URL(ApplicationParameters.MAIN_DOMAIN + intent.getStringExtra("url"));
            HashMap<String, String> vars = (HashMap<String, String>)intent.getSerializableExtra("vars");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);


            String device_info = "";
            device_info += " OS Version: " + System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
            device_info += "\n OS API Level: " + android.os.Build.VERSION.RELEASE + "(" + android.os.Build.VERSION.SDK_INT + ")";
            device_info += "\n Device: " + android.os.Build.DEVICE;
            device_info += "\n Model (and Product): " + android.os.Build.MODEL + " (" + android.os.Build.PRODUCT + ")";
            vars.put("device_info", device_info);
            String login = "";// context.getSharedPreferences("UserPreference", 0).getString("login", "");
            if (!login.equals("")) vars.put("login", login);


            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(vars));
            writer.flush();
            writer.close();
            os.close();
            int responseCode=urlConnection.getResponseCode();

            InputStream is;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String line;
                is = urlConnection.getInputStream();
                BufferedReader br= new BufferedReader(new InputStreamReader(
                        is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                response = new String(
                        new String(
                                sb.toString().getBytes("ISO-8859-1"), "windows-1251"
                        ).getBytes(), "UTF-8"
                );
            }

            data.putString("result", response);
            data.putInt("status_code", responseCode);
            data.putInt("method", intent.getIntExtra("method", -1));
            receiver.send(Constants.STATUS_SUCCESS, data);
        } catch (Exception e) {
            e.printStackTrace();
            receiver.send(Constants.STATUS_ERROR, Bundle.EMPTY);
        }
    }
}
