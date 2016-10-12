package com.logistic.paperrose.mttp.oldversion.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.logistic.paperrose.mttp.R;

/**
 * Created by paperrose on 01.02.2015.
 */
public class TabViewAdapter extends ArrayAdapter<Integer> {

    Integer[] ids;
    LayoutInflater lInflater;
    public TabViewAdapter(Context context, int textViewResourceId, Integer[] objects) {
        super(context, textViewResourceId, objects);
        ids = objects;
        lInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return ids.length;
    }

    // элемент по позиции
    @Override
    public Integer getItem(int position) {
        return ids[position];
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.tab_view, parent, false);
        }
        return view;
    }
}
