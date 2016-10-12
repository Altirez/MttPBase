package com.logistic.paperrose.mttp.oldversion.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.settings.SingleField;
import com.logistic.paperrose.mttp.oldversion.utils.ColoredArrayAdapter;

import java.util.ArrayList;

/**
 * Created by paperrose on 27.01.2015.
 * адаптер для списка фильтров
 */
public class FilteringOptionsAdapter extends ColoredArrayAdapter<TripleTableField> {

    ArrayList<View> views = new ArrayList<View>();
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<TripleTableField> chosenObjects = new ArrayList<TripleTableField>();
    ArrayList<TripleTableField> objects = new ArrayList<TripleTableField>();

    public FilteringOptionsAdapter(Context context, int textViewResourceId, ArrayList<TripleTableField> objects) {
        super(context, textViewResourceId, objects);
        ctx = context;
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.objects = objects;
    }

    @Override
    public int getCount() {
        return chosenObjects.size();
    }

    public ArrayList<SingleField> getSingleFields() {
        ArrayList<SingleField> arr = new ArrayList<SingleField>();
        for (int i = 0; i < objects.size(); i++) {
            boolean addThis = true;
            for (int j = 0; j < chosenObjects.size(); j++) {
                if (objects.get(i).getName().equals(chosenObjects.get(j).getName())) {
                    addThis = false;
                    break;
                }
            }
            if (addThis) {
                arr.add(objects.get(i));
            }
        }
        return arr;
    }

    public ArrayList<TripleTableField> getTableFields() {
        ArrayList<TripleTableField> arr = new ArrayList<TripleTableField>();
        for (int i = 0; i < objects.size(); i++) {
            boolean addThis = true;
            for (int j = 0; j < chosenObjects.size(); j++) {
                if (objects.get(i).getName().equals(chosenObjects.get(j).getName())) {
                    addThis = false;
                    break;
                }
            }
            if (addThis) {
                arr.add(objects.get(i));
            }
        }
        return arr;
    }
    // элемент по позиции
    @Override
    public TripleTableField getItem(int position) {
        return chosenObjects.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    public void removeObject(int position) {
        chosenObjects.remove(position);
        refresh();
    }

    public void removeObject(TripleTableField object) {
        chosenObjects.remove(object);
        refresh();
    }

    public void addObject(TripleTableField object) {
        chosenObjects.add(object);
        refresh();
    }

    public void addObject() {
        chosenObjects.add(new TripleTableField());
        refresh();
    }

    public void addObject(int position) {
        chosenObjects.add(getTableFields().get(position));
        refresh();
    }

    public void refresh() {
        views.clear();
        notifyDataSetChanged();
    }


    public TripleTableField getTableFieldByText(String text) {
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).getText().equals(text)) {
                return objects.get(i);
            }
        }
        return null;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View f_view;
        if (views.size() < chosenObjects.size()) {
            f_view = lInflater.inflate(R.layout.filter_fields_list_item, parent, false);
            final DelayedFilteringField dff = (DelayedFilteringField)f_view.findViewById(R.id.columnKey);
            ImageButton delete = (ImageButton)f_view.findViewById(R.id.deleteButton);
            FilteringFieldAdapter adapter = new FilteringFieldAdapter(ctx, R.layout.auto_tablefield, getSingleFields());
            if (!chosenObjects.get(position).getName().isEmpty()) {
                dff.setText(chosenObjects.get(position).getText());
            }
            dff.setAdapter(adapter);
            dff.setThreshold(1);

            dff.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    TripleTableField tmp = getTableFieldByText(((TextView)view).getText().toString());
                    chosenObjects.get(position).setName(tmp.getName());
                    chosenObjects.get(position).setText(tmp.getText());
                    chosenObjects.get(position).setType(tmp.getType());
                    refresh();
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeObject(position);
                }
            });
            views.add(f_view);
        } else {
            f_view = views.get(position);
        }
        return f_view;
    }
}
