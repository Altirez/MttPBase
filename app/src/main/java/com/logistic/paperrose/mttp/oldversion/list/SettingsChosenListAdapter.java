package com.logistic.paperrose.mttp.oldversion.list;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.dragndrop.DropListener;
import com.logistic.paperrose.mttp.oldversion.dragndrop.RemoveListener;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;

import java.util.ArrayList;

/**
 * Created by paperrose on 23.04.2015.
 */
public class SettingsChosenListAdapter extends BaseChosenListAdapter implements DropListener, RemoveListener {



    public SettingsChosenListAdapter(Context context, int textViewResourceId, ArrayList<TripleTableField> objects) {
        super(context, textViewResourceId, objects);
    }

    class ViewHolder {
        ImageView tv;
        CheckBox cb;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        if (convertView == null) {
            convertView = lInflater.inflate(R.layout.text_edit_list_layout_ver2, null);
            holder = new ViewHolder();
            holder.cb = (CheckBox) convertView.findViewById(R.id.field);

            holder.tv = (ImageView) convertView.findViewById(R.id.upItem);
            holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    int getPosition = (Integer) compoundButton.getTag();
                    if (!b) {
                        TripleTableField tf = getItem(getPosition);
                        items.remove(tf);
                        flAdapter.refresh();
                        notifyDataSetChanged();
                    }
                }
            });
            convertView.setTag(holder);
            convertView.setTag(R.id.field, holder.cb);
            convertView.setTag(R.id.upItem, holder.tv);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.cb.setTag(position);
        holder.tv.setTag(position);
        holder.cb.setText((getItem(position) != null ? getItem(position).getText() : ""));
        holder.cb.setChecked(true);
        int colorPos = position % ApplicationParameters.colors.length;
        convertView.setBackgroundColor(ApplicationParameters.colors[colorPos]);
//        convertView = super.getView(position, convertView, parent);
        return convertView;
    }

    @Override
    public void onRemove(int which) {
        items.remove(which);
        notifyDataSetChanged();
    }

    @Override
    public void onDrop(int from, int to) {
        TripleTableField temp =  items.get(from);
        items.remove(from);
        items.add(to, temp);
        notifyDataSetChanged();
    }
}
