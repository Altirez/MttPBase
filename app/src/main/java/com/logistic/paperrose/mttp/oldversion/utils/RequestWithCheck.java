package com.logistic.paperrose.mttp.oldversion.utils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Pair;
import android.widget.Toast;

import com.logistic.paperrose.mttp.oldversion.LoginActivity;
import com.logistic.paperrose.mttp.oldversion.MainActivity;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by paperrose on 30.01.2015.
 */
public class RequestWithCheck extends AsyncTask<Pair<String, HashMap<String, String>>, Void, JSONObject> {

    public void setActivity(ConnectionActivity activity) {
        this.activity = activity;

    }

    ConnectionActivity activity;

    public void setHandler(ExecutionHandler handler) {
        this.handler = handler;
    }

    ExecutionHandler handler;

    @Override
    protected JSONObject doInBackground(Pair<String, HashMap<String, String>>... args) {
        JSONObject jsonObject = null;
        try {
            jsonObject = activity.checkServerCredentials(args[0].first, args[0].second);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    protected void onPostExecute(JSONObject res) {
        activity.result = res;
        activity.showProgress(false);
        try {
            if (res == null) {
                Toast.makeText(activity, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
            }
            else
            if (res.has("error")) {
                if (res.getInt("status_code") == 401) {
                    if (!ApplicationParameters.loginOpened) {
                        ApplicationParameters.loginOpened = true;
                        Intent intent = new Intent(activity, LoginActivity.class);
                        intent.putExtra("login", true);
                        activity.startActivity(intent);
                        activity.finish();
                    }
                } else {
                    Toast.makeText(activity.getApplicationContext(), res.getString("error"), Toast.LENGTH_LONG).show();
                }
            } else {
                SharedPreferences prefs = activity.getSharedPreferences("UserPreference", 0);
                try {
                    MainActivity.saveCredentials(res.getString("access_token"), "access_token", prefs);
                } catch (Exception e) {

                }
                if (handler != null)
                    handler.Execute();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
