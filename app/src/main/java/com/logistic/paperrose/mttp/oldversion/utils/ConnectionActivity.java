package com.logistic.paperrose.mttp.oldversion.utils;

import com.logistic.paperrose.mttp.oldversion.BaseLogisticActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by paperrose on 30.01.2015.
 */
public class ConnectionActivity extends BaseLogisticActivity {
    public JSONObject result;

    public JSONObject checkServerCredentials(String url, HashMap<String, String> args) throws JSONException {
        JSONParser parser = new JSONParser();
        JSONObject obj = parser.getJSONFromUrl(url, args, getApplicationContext());
        return obj;
    }

    public String getJSONParameters() {
        return "";
    }

    protected void Execute() {

    }
}
