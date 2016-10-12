package com.logistic.paperrose.mttp.newversion.network;

import android.content.ContentValues;

import com.logistic.paperrose.mttp.newversion.json.Constants;
import com.squareup.otto.Produce;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by paperrose on 01.07.2016.
 */
public class CommunicatorCanavara {

    private static final String TAG = "CommunicatorCanavara";
    private static final String SERVER_ADDRESS = "http://212.68.8.90";
    private static final String MAIN_DOMAIN = SERVER_ADDRESS + "/api/index.php/";
    private static String device_info;
    private static String access_token;
    private static String login;
    private static Retrofit retrofit;

    public CommunicatorCanavara() {
        retrofit = new Retrofit.Builder()
                .baseUrl(MAIN_DOMAIN)
                .build();

        login = "SYSDBA";

        access_token = "";

        device_info = "";
        device_info += " OS Version: " + System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
        device_info += "\n OS API Level: "+android.os.Build.VERSION.RELEASE + "("+android.os.Build.VERSION.SDK_INT+")";
        device_info += "\n Device: " + android.os.Build.DEVICE;
        device_info += "\n Model (and Product): " + android.os.Build.MODEL + " ("+ android.os.Build.PRODUCT + ")";
    }

    @Produce
    public ResponseEvent produceServerEvent(ResponseBody serverResponse, Constants.Methods method) {
        return new ResponseEvent(serverResponse, method);
    }

    @Produce
    public ErrorEvent produceErrorEvent(int errorCode, String errorMsg) {
        return new ErrorEvent(errorCode, errorMsg);
    }


    public void communicate(Constants.Methods method, ContentValues vars) {
        switch (method.name()) {
            case "LOAD_INFO":
                userAvailableFieldsPost();
        }
    }

    public Callback<ResponseBody> commonCommunicate(final Constants.Methods method) {
        return new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200)
                    BusProvider.getInstance().post(produceServerEvent(response.body(), method));
                else
                    BusProvider.getInstance().post(produceErrorEvent(response.code(), response.errorBody().toString()));
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                BusProvider.getInstance().post(produceErrorEvent(-200, t.getMessage()));
            }
        };
    }




    public void userAvailableFieldsPost(){
        RequestInterface communicatorInterface = retrofit.create(RequestInterface.class);
        Call<ResponseBody> response = communicatorInterface.userAvailableFieldsPost(access_token, login, device_info);
        response.enqueue(commonCommunicate(Constants.Methods.LOAD_INFO));
    }

    public void userAvailableFieldsGet(){
        RequestInterface communicatorInterface = retrofit.create(RequestInterface.class);
        Call<ResponseBody> response = communicatorInterface.userAvailableFieldsGet(access_token, login, device_info);
        response.enqueue(commonCommunicate(Constants.Methods.LOAD_INFO));
    }



}
