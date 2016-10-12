package com.logistic.paperrose.mttp.newversion.network;

import com.logistic.paperrose.mttp.newversion.json.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import okhttp3.ResponseBody;

/**
 * Created by paperrose on 01.07.2016.
 */
public class ResponseEvent {

    private JSONObject responseObject;
    private Constants.Methods method;

    public Constants.Methods getMethod() {
        return method;
    }

    public void setMethod(Constants.Methods method) {
        this.method = method;
    }

    public JSONObject getResponseObject() {
        return responseObject;
    }

    public void setRequestBody(JSONObject responseObject) {
        this.responseObject = responseObject;
    }

    public ResponseEvent(ResponseBody responseBody, Constants.Methods method) {
        try {
            String json = null;
            BufferedReader reader = null;
            InputStream is = responseBody.byteStream();
            reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
            json = new String(new String(json.getBytes("ISO-8859-1"), "windows-1251").getBytes(), "UTF-8");
            this.responseObject = new JSONObject(json);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.method = method;
    }
}
