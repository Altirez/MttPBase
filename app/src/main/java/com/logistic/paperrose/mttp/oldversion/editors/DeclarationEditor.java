package com.logistic.paperrose.mttp.oldversion.editors;

import android.os.Bundle;
import android.app.Activity;

import com.logistic.paperrose.mttp.R;

public class DeclarationEditor extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_declaration_editor);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
