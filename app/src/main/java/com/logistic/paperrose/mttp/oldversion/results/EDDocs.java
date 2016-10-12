package com.logistic.paperrose.mttp.oldversion.results;

import android.os.Bundle;
import android.widget.ListView;

import com.logistic.paperrose.mttp.oldversion.BaseLogisticActivity;
import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;

import java.util.ArrayList;
import java.util.Random;

public class EDDocs extends BaseLogisticActivity {
    String num;
    String filename;
    ListView listView1;
    ArrayList<TripleTableField> items;
    EDAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setLayoutId(R.layout.activity_ed_docs);
        super.onCreate(savedInstanceState);
        num = getIntent().getStringExtra("number");
        //setContentView(R.layout.activity_download_documents);
        listView1 = (ListView) findViewById(R.id.edsList);
        refresh();

    }

    public void refresh() {
        items = ApplicationParameters.eds.get(num) != null ? ApplicationParameters.eds.get(num) : new ArrayList<TripleTableField>();
        adapter = new EDAdapter(this, items);
        listView1.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    protected String getRandomName(String ext) {
        String s = "document";
        Random rand = new Random();
        for (int i = 0; i < 10; i++)
            s += Integer.toString(rand.nextInt(10));
        s += "." + ext;
        return s;
    }
}
