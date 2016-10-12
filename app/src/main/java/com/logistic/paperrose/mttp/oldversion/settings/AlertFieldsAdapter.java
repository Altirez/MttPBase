package com.logistic.paperrose.mttp.oldversion.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;

import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.google.gson.Gson;
import com.logistic.paperrose.mttp.oldversion.utils.ColoredArrayAdapter;

import java.util.ArrayList;

/**
 * Created by paperrose on 12.01.2015.
 */
public class AlertFieldsAdapter extends ColoredArrayAdapter<TripleTableField> implements Filterable {

    public ChosenFieldsAdapterVer2 secondListAdapter = null;
    private ArrayList<TripleTableField> filteredItems = null;
    private ItemFilter mFilter = new ItemFilter();
    public CharSequence filterS = null;
    public ArrayList<TripleTableField> mainList;
    public ArrayList<TripleTableField> chosenList;
    public int textViewResourceId = R.layout.text_edit_list_layout3;
    public int type = 0;
    public ListView lv = null;
    public AlertFieldsAdapter(Context context, int textViewResourceId, ArrayList<TripleTableField> objects, ListView lv, ArrayList<TripleTableField> list1, ArrayList<TripleTableField> list2) {
        super(context, textViewResourceId, objects);
        ctx = context;
        this.lv = lv;
        this.objects = objects;
        mainList = list1;
        chosenList = list2;
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        filteredItems = getTableFields();

    }

    public AlertFieldsAdapter(Context context, int textViewResourceId, ArrayList<TripleTableField> objects, ListView lv, ArrayList<TripleTableField> list1, ArrayList<TripleTableField> list2, int textViewResourceId2) {
        super(context, textViewResourceId, objects);
        ctx = context;
        this.lv = lv;
        this.objects = objects;
        this.textViewResourceId = textViewResourceId2;
        mainList = list1;
        chosenList = list2;
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        filteredItems = getTableFields();

    }




    public void saveChanges(SharedPreferences prefs,String saveType) {
        SharedPreferences historyPrefs = prefs;// = getSharedPreferences("UserPreference", 0);
        SharedPreferences.Editor prefsEditor = historyPrefs.edit();
        Gson gson = new Gson();
        String json1 = gson.toJson(mainList);
        String json2 = gson.toJson(chosenList);
        if (type == 0) {
            prefsEditor.putString("all", json1);

        }  else {
            prefsEditor.putString("declarations", json1);
        }
        prefsEditor.putString(saveType, json2);
        prefsEditor.commit();
        //+ запрос на сервер
    }

    public Context ctx;
    public LayoutInflater lInflater;
    public ArrayList<TripleTableField> objects = new ArrayList<TripleTableField>();
    public ArrayList<View> views = new ArrayList<View>();

    public ArrayList<TripleTableField> getUsedObjects() {
        return objects;
    }

    public void setUsedObjects(ArrayList<TripleTableField> usedObjects) {
        this.usedObjects = usedObjects;
        notifyDataSetChanged();
    }

    ArrayList<TripleTableField> usedObjects = new ArrayList<TripleTableField>();

    // кол-во элементов
    @Override
    public int getCount() {
      //  if (filteredItems.size() > 0) lv.setVisibility(View.VISIBLE);
      //  else lv.setVisibility(View.GONE);
        return filteredItems.size();
  //      return objects.size() - ApplicationParameters.chosenTableFields.size();
    }

    // элемент по позиции
    @Override
    public TripleTableField getItem(int position) {
        return filteredItems.get(position);
        //return getTableFields().get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    public ArrayList<TripleTableField> getTableFields() {
        ArrayList<TripleTableField> arr = new ArrayList<TripleTableField>();
        for (int i = 0; i < mainList.size(); i++) {
            boolean addThis = true;
            for (int j = 0; j < chosenList.size(); j++) {
                if (mainList.get(i).getName().equals(chosenList.get(j).getName())) {
                    addThis = false;
                    break;
                }
            }
            if (addThis) {
                arr.add(mainList.get(i));
            }
        }
        return arr;
    }



    // пункт списка
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null || views.size() < filteredItems.size()) {// objects.size() - ApplicationParameters.chosenTableFields.size()) {
            view = lInflater.inflate(textViewResourceId, parent, false);
            final CheckBox tw = (CheckBox)view.findViewById(R.id.field);
            TripleTableField p = getItem(position);
            tw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    ApplicationParameters.addChosenField(getItem(position), chosenList);
                    saveChanges(ctx.getSharedPreferences("UserPreference", 0), secondListAdapter.saveType);
                    if (filterS != null) {
                        getFilter().filter(filterS.toString());
                    } else {
                        getFilter().filter("");
                    }

                    views.clear();
                    secondListAdapter.views.clear();
                    secondListAdapter.notifyDataSetChanged();
                    notifyDataSetChanged();

                }
            });
            view.setVisibility(tw.isChecked() ? View.GONE : View.VISIBLE);
            tw.setText(p.getText());
            views.add(view);
        } else {
            view = views.get(position);
        }
        int colorPos = position % ApplicationParameters.colors.length;
        view.setBackgroundColor(ApplicationParameters.colors[colorPos]);
        return view;
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<TripleTableField> list = getTableFields();

            int count = list.size();
            final ArrayList<TripleTableField> nlist = new ArrayList<TripleTableField>(count);

            TripleTableField filterableString ;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i);
                if (filterableString.getText().toLowerCase().contains(filterString)) {
                    nlist.add(filterableString);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredItems = (ArrayList<TripleTableField>) results.values;
            views.clear();
            notifyDataSetChanged();
        }

    }
}
