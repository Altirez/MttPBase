package com.logistic.paperrose.mttp.newversion.json;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by paperrose on 29.06.2016.
 */

@SuppressLint("ParcelCreator")
public class JsonResultReceiver extends ResultReceiver {

    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle data);
    }

    private Receiver mReceiver;

    public JsonResultReceiver(Handler handler) {
        super(handler);
    }

    public Receiver getReceiver() {
        return mReceiver;
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }
}
