package com.logistic.paperrose.mttp.oldversion.list;

import android.content.Context;
import android.view.LayoutInflater;

import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.utils.ColoredArrayAdapter;

import java.util.ArrayList;

/**
 * Created by paperrose on 23.04.2015.
 */
public class BaseChosenListAdapter extends ColoredArrayAdapter<TripleTableField> {


    public ArrayList<TripleTableField> items;
    public FieldsListAdapter flAdapter;
    Context context;
    public BaseChosenListAdapter(Context context, int textViewResourceId, ArrayList<TripleTableField> objects) {
        super(context, textViewResourceId, objects);
        items = objects;
        this.context = context;
        lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    // элемент по позиции
    @Override
    public TripleTableField getItem(int position) {
        return items.get(position);
    }


    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    public LayoutInflater lInflater;


}
