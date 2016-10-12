package com.logistic.paperrose.mttp.newversion.json;

import android.os.Bundle;

/**
 * Created by paperrose on 29.06.2016.
 */
public class JsonParser implements JsonResultReceiver.Receiver {

    @Override
    public void onReceiveResult(int resultCode, Bundle data) {
        switch (resultCode) {
            case Constants.STATUS_STARTED:

                break;
            case Constants.STATUS_SUCCESS:
                onSuccess(data);
                break;
            case Constants.STATUS_ERROR:

                break;
            default:
                break;
        }
    }

    public void onSuccess(Bundle data) {
        Constants.Methods method = (Constants.Methods)data.getSerializable("method");
        switch (method.name()) {
            case "AUTH":
                break;
            case "LOGOUT":
                break;
            case "LOAD_INFO":
                break;
            case "GET_PUSHES":
                break;
            case "GET_ED":
                break;
            case "SEARCH_TRAFFIC":
                break;
            case "SEARCH_TRAFFIC_BY_NUM":
                break;
            case "SEARCH_GTD":
                break;
            case "SEARCH_GTD_BY_NUM":
                break;
            case "AUTOCOMPLETE":
                break;
            case "SEARCH_DICT":
                break;
            case "EDIT_TRAFFIC":
                break;
            case "EDIT_GTD":
                break;
            case "DICT":
                break;
            case "ADD_BOOKMARK":
                break;
            case "EDIT_BOOKMARK":
                break;
            case "EDIT_BOOKMARK_ORDER":
                break;
            case "CHECK_AUTH":
                break;
            case "SAVE_SETTINGS":
                break;
            case "UPLOAD_DOCUMENT":
                break;
        }
    }

}
