package com.logistic.paperrose.mttp.oldversion.contacts;

import android.os.Bundle;

import com.logistic.paperrose.mttp.oldversion.BaseLogisticActivity;
import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;


public class ContactsActivity extends BaseLogisticActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLayoutId(R.layout.activity_contacts);
        ApplicationParameters.chosenDeclarationType = "all";
        super.onCreate(savedInstanceState);
    }

}
