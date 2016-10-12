package com.logistic.paperrose.mttp.oldversion.clients;

import android.content.Context;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.TextView;

import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.dictionaries.DictViewPagerSearchActivity;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;
import com.logistic.paperrose.mttp.oldversion.utils.ColoredArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by paperrose on 04.02.2015.
 */
public class ClientsAdapter extends ColoredArrayAdapter<String> {

    private ArrayList<String> clients;
    public ArrayList<Boolean> selected;

    Context context;

    public ClientsAdapter(Context context, int textViewResourceId, ArrayList<String> objects, String type) {
        super(context, textViewResourceId, objects);
        this.clients = objects;
        statusTest = type;
        this.context = context;
        selected = new ArrayList<Boolean>(clients.size());
        for (int i = 0; i < clients.size(); i++) {
            selected.add(false);
        }
        filteredItems = new ArrayList<String>(objects);
        lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }


    @Override
    public int getCount() {
        return filteredItems.size();
    }

    // элемент по позиции
    @Override
    public String getItem(int position) {
        return filteredItems.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getItemNum(String item) {

        for (int j = 0; j < clients.size(); j++) {
            if (clients.get(j).equals(item)) {
                return j;
            }
        }
        return -1;
    }

    public LayoutInflater lInflater;

    private class ViewHolder {
        TextView tw;
        CheckBox cb;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if (convertView == null) {

            convertView = lInflater.inflate(R.layout.client_view, null);

            holder = new ViewHolder();

            holder.cb = (CheckBox) convertView.findViewById(R.id.isChecked);
            holder.tw = (TextView) convertView.findViewById(R.id.name);
            holder.tw.setClickable(true);
            holder.tw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int getPosition = getItemNum(getItem((Integer) view.getTag()));
                //    ((ClientsActivity)context).showProgress(true);
                    Intent intent = new Intent(context, DictViewPagerSearchActivity.class);
                    intent.putExtra("page_num", getPosition);
                    intent.putExtra("statusTest", statusTest);
                    context.startActivity(intent);
                }
            });

            holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    int getPosition = (Integer) compoundButton.getTag();
                    selected.set(getPosition, b);
                }
            });
            convertView.setTag(holder);
            convertView.setTag(R.id.isChecked, holder.cb);
            convertView.setTag(R.id.name, holder.tw);
        } else

        {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.cb.setTag(position);
        holder.tw.setTag(position);
        holder.tw.setText(

                getItem(position)

        );
        holder.cb.setChecked(selected.get(position));
        int colorPos = position % ApplicationParameters.colors.length;
        convertView.setBackgroundColor(ApplicationParameters.colors[colorPos]);
        return convertView;
    }

    public void onCreateContextMenu(ContextMenu contextMenu, View view,  ContextMenu.ContextMenuInfo contextMenuInfo) {
        // empty implementation
    }

    public String statusTest = "LIST_CLIENT_NAME";


    public String getJSONParameters(String chosenStatus) {
        String mainF = "";
        JSONArray arr = new JSONArray();
        JSONObject obj = new JSONObject();
        try {
            obj.put("field_name", statusTest);
            obj.put("field_type", "dict");
            obj.put("field_value", chosenStatus);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        arr.put(obj);

        JSONObject res = new JSONObject();
        try {
            res.put("main_filter", mainF);
            res.put("filters", arr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApplicationParameters.lastSearchString = res;
        return res.toString();
    }


    public String getPref(String key) {
        return context.getSharedPreferences("UserPreference", 0).getString(key, "123");
    }

    public String status;

    private ItemFilter mFilter = new ItemFilter();
    public Filter getFilter() {
        return mFilter;
    }
    private ArrayList<String> filteredItems = null;
    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<String> list = clients;

            int count = list.size();
            final ArrayList<String> nlist = new ArrayList<String>(count);

            String filterableString ;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i);
                if (filterableString.toLowerCase().contains(filterString)) {
                    nlist.add(filterableString);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
            filteredItems = (ArrayList<String>) results.values;
            notifyDataSetChanged();
        }

    }


}