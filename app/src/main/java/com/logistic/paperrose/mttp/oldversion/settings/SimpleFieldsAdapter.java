package com.logistic.paperrose.mttp.oldversion.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.utils.ColoredArrayAdapter;

import java.util.ArrayList;

/**
 * Created by paperrose on 16.03.2015.
 */
public class SimpleFieldsAdapter extends ColoredArrayAdapter<TripleTableField> {
    ArrayList<TripleTableField> unselectedFields = new ArrayList<TripleTableField>();
    //BaseLogisticActivity activity = null;
    public SelectedFieldsAdapter selectedFieldsAdapter;
    LayoutInflater lInflater;
    Context ctx;
    public SimpleFieldsAdapter(Context context, int textViewResourceId, ArrayList<TripleTableField> objects) {
        super(context, textViewResourceId, objects);
        unselectedFields = objects;
        ctx = context;
        lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    class SimpleFieldHolder {
        CheckBox cb;
    }

    public void addItem(TripleTableField item) {
        unselectedFields.add(item);
        selectedFieldsAdapter.selectedFields.remove(item);
        notifyDataSetChanged();
        selectedFieldsAdapter.notifyDataSetChanged();
    }

    public void removeItem(TripleTableField item) {
        selectedFieldsAdapter.selectedFields.add(item);
        unselectedFields.remove(item);
        notifyDataSetChanged();
        selectedFieldsAdapter.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return unselectedFields.size();
    }

    // элемент по позиции
    @Override
    public TripleTableField getItem(int position) {
        return unselectedFields.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SimpleFieldHolder holder = null;

        if (convertView == null) {
            convertView = lInflater.inflate(R.layout.text_edit_list_layout, null);
            holder = new SimpleFieldHolder();
            holder.cb = (CheckBox) convertView.findViewById(R.id.isChecked);
            holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    int getPosition = (Integer) compoundButton.getTag();
                    removeItem(getItem(getPosition));
                }
            });
            convertView.setTag(holder);
            convertView.setTag(R.id.field, holder.cb);

        } else {
            holder = (SimpleFieldHolder) convertView.getTag();
        }
        holder.cb.setTag(position);
        holder.cb.setChecked(false);
        return convertView;
    }

}
