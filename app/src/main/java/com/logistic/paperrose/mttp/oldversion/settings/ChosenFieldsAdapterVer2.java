package com.logistic.paperrose.mttp.oldversion.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.dragndrop.DragNDropListView;
import com.logistic.paperrose.mttp.oldversion.dragndrop.DropListener;
import com.logistic.paperrose.mttp.oldversion.dragndrop.RemoveListener;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.google.gson.Gson;
import com.logistic.paperrose.mttp.oldversion.utils.ColoredArrayAdapter;

import java.util.ArrayList;

/**
 * Created by paperrose on 12.01.2015.
 */
public class ChosenFieldsAdapterVer2 extends ColoredArrayAdapter<TripleTableField> implements RemoveListener, DropListener {

    public void setChosen(boolean isChosen) {
        notifyDataSetChanged();
    }

    public ChosenFieldsAdapter firstListAdapter = null;
    public ArrayList<View> views = new ArrayList<View>();

    public ArrayList<TripleTableField> chosenList;
    public String saveType = "selected";

    public DragNDropListView lv = null;

    public void saveChanges(SharedPreferences prefs) {
        SharedPreferences historyPrefs = prefs;// = getSharedPreferences("UserPreference", 0);
        SharedPreferences.Editor prefsEditor = historyPrefs.edit();
        Gson gson = new Gson();
        String json2 = gson.toJson(chosenList);
        String json1 = gson.toJson(ApplicationParameters.tableFields);
        prefsEditor.putString("all", json1);
        String json3 = gson.toJson(ApplicationParameters.declarationFields);
        prefsEditor.putString("declarations", json3);
        prefsEditor.putString(saveType, json2);
        prefsEditor.commit();
    }

    public ChosenFieldsAdapterVer2(Context context, int textViewResourceId, ArrayList<TripleTableField> objects, DragNDropListView lv, ArrayList<TripleTableField> chosen) {
        super(context, textViewResourceId, objects);
        ctx = context;
        this.lv = lv;
        this.chosenList = chosen;
        this.objects = new ArrayList<TripleTableField>(objects);
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }


    public ArrayList<AutoCompleteTextView> tws = new ArrayList<AutoCompleteTextView>();
    public Context ctx;
    public LayoutInflater lInflater;
    public ArrayList<TripleTableField> objects = new ArrayList<TripleTableField>();

    // кол-во элементов
    @Override
    public int getCount() {
        if (chosenList.size() > 0) lv.setVisibility(View.VISIBLE);
        else lv.setVisibility(View.GONE);
        return chosenList.size();
    }

    // элемент по позиции
    @Override
    public TripleTableField getItem(int position) {
        return chosenList.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }
    public void addItem(TripleTableField item) {

    }
    // пункт списка
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view

        View view = convertView;
        if (view == null  || views.size() < chosenList.size()) {
            view = lInflater.inflate(R.layout.text_edit_list_layout_ver2, parent, false);
            final CheckBox tw = (CheckBox)view.findViewById(R.id.field);

            TripleTableField p = getItem(position);
            tw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        try {
                            chosenList.remove(position);

                            if (firstListAdapter.filterS != null) {
                                firstListAdapter.getFilter().filter(firstListAdapter.filterS.toString());
                            } else {
                                firstListAdapter.getFilter().filter("");
                            }
                            views.clear();
                            firstListAdapter.views.clear();
                            firstListAdapter.notifyDataSetChanged();
                            firstListAdapter.saveChanges(ctx.getSharedPreferences("UserPreference", 0), saveType);
                            notifyDataSetChanged();
                        } catch (IndexOutOfBoundsException e) {

                        }
                }
            });
            ImageView up = (ImageView)view.findViewById(R.id.upItem);

            tw.setText(p.getText());
            views.add( view);
        } else {
            view = views.get(position);
        }
        int colorPos = position % ApplicationParameters.colors.length;
        view.setBackgroundColor(ApplicationParameters.colors[colorPos]);
     //   view = super.getView(position, view, parent);
       // holder.cb.setChecked(true);
        return view;
    }

    @Override
    public void onRemove(int which) {
        if (which < 0 || which > chosenList.size()-1) return;
        chosenList.remove(which);
        views.clear();
        firstListAdapter.saveChanges(ctx.getSharedPreferences("UserPreference", 0));
        notifyDataSetChanged();
    }

    @Override
    public void onDrop(int from, int to) {
        TripleTableField temp =  chosenList.get(from);
        chosenList.remove(from);
        chosenList.add(to, temp);
        views.clear();
        firstListAdapter.saveChanges(ctx.getSharedPreferences("UserPreference", 0));
        notifyDataSetChanged();
    }
}
