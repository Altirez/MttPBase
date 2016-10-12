package com.logistic.paperrose.mttp.oldversion.reports;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;

import com.logistic.paperrose.mttp.oldversion.BaseLogisticActivity;
import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.newsearch.FiltersAdapter;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paperrose on 11.08.2015.
 */
public class ReportSettingsActivity extends BaseLogisticActivity {
    public ArrayList<TripleTableField> groupFields = new ArrayList<TripleTableField>();
    FiltersAdapter reportFiltersChosenAdapter;
    String fieldsOrder = "";
    List<View> pages = new ArrayList<View>();
    ViewPager viewPager;
    LayoutInflater inflater;
    public String reportID = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLayoutId(R.layout.activity_report_settings);
        super.onCreate(savedInstanceState);
        reportID = getIntent().getStringExtra("reportID");

    }

    public void createPageOrder() throws JSONException {
        View page = inflater.inflate(R.layout.activity_test, null);
        pages.add(page);
    }

    public void createPageFilter() throws JSONException {
        View page = inflater.inflate(R.layout.activity_filter_reports, null);
        pages.add(page);
    }

    public void createPageGroup() throws JSONException {
        View page = inflater.inflate(R.layout.activity_test, null);
        pages.add(page);
    }

    //order, group, filter
}
