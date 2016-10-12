package com.logistic.paperrose.mttp.oldversion.results;

import android.app.Activity;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.utils.ColoredArrayAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by paperrose on 03.02.2016.
 */
public class EDAdapter extends ColoredArrayAdapter<TripleTableField> {
    EDDocs context;
    ArrayList<TripleTableField> items;

    public EDAdapter(Activity context, ArrayList<TripleTableField> items) {
        super(context, R.layout.ed_item, items);
        this.context = (EDDocs)context;
        this.items = items;
    }

    @Override
    public int getCount() {

        return items.size();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public TripleTableField getItem(int i) {
        return items.get(i);
    }

    public void refresh(ArrayList<TripleTableField> items)
    {
        this.items = items;
        notifyDataSetChanged();
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        LayoutInflater inflater = context.getLayoutInflater();
        if (view == null) {
            view = inflater.inflate(R.layout.ed_item, viewGroup, false);
        }
        TripleTableField p = items.get(i);
        Spanned datetext = Html.fromHtml("<b>" + timeFromTimestamp(p.getName(), 1000) + "</b> " + dateFromTimestamp(p.getName(), 1000));
        ((TextView) view.findViewById(R.id.datetime)).setText(datetext);
        ((TextView) view.findViewById(R.id.description)).setText(p.getText());
        return view;
    }

    private String dateFromTimestamp(String timestamp, int multiplier) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+4"));
        return dateFormat.format((new Date(multiplier*Long.parseLong(timestamp))));
    }

    private String timeFromTimestamp(String timestamp, int multiplier) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+4"));
        return dateFormat.format((new Date(multiplier*Long.parseLong(timestamp))));
    }

}
