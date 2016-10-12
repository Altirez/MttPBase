package com.logistic.paperrose.mttp.oldversion.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;

import java.util.ArrayList;

/**
 * Created by paperrose on 15.03.2016.
 */
public class ColoredArrayAdapter<T> extends ArrayAdapter<T> {
    private int[] colors = new int[] { 0xffffffff, 0xffededee };
    int resource;
    public Context context;
    public ArrayList<T> items;

    public ColoredArrayAdapter(Context context, int resource, ArrayList<T> items) {
        super(context, resource, items);
        this.resource = resource;
        this.context = context;
        this.items =items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        //View view = context.getLayoutInflater().inflate(resource, null);
        int colorPos = position % ApplicationParameters.colors.length;
        view.setBackgroundColor(ApplicationParameters.colors[colorPos]);
        return view;
    }
}
