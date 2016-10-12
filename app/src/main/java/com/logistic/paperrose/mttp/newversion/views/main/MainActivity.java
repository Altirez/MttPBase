package com.logistic.paperrose.mttp.newversion.views.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.newversion.json.Constants;
import com.logistic.paperrose.mttp.newversion.network.BusProvider;
import com.logistic.paperrose.mttp.newversion.network.CommunicatorCanavara;
import com.logistic.paperrose.mttp.newversion.network.ErrorEvent;
import com.logistic.paperrose.mttp.newversion.network.ResponseEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * Created by paperrose on 28.06.2016.
 */
public class MainActivity extends AppCompatActivity{
    private CommunicatorCanavara communicator;
    private Button afButton;
    private final static String TAG = "MainActivity";
    public static Bus bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_activity_test);

        communicator = new CommunicatorCanavara();
        afButton = (Button)findViewById(R.id.testButton);
        afButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                communicator.communicate(Constants.Methods.LOAD_INFO, null);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onResponseEvent(ResponseEvent responseEvent){

    }

    @Subscribe
    public void onErrorEvent(ErrorEvent errorEvent){
        Toast.makeText(this,""+errorEvent.getErrorMsg(),Toast.LENGTH_SHORT).show();
    }

}
