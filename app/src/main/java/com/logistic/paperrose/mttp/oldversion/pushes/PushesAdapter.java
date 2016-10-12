package com.logistic.paperrose.mttp.oldversion.pushes;

import android.app.Activity;
import android.content.SharedPreferences;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;


/**
 * Created by paperrose on 19.12.2014.
 */
public class PushesAdapter extends ArrayAdapter<PushItem> {
    MyActivity context;
    ArrayList<PushItem> items;
    private ArrayList<PushItem> filteredItems = null;
    public int currentOffset;
    public String currentType;

    public PushesAdapter(Activity context, ArrayList<PushItem> items) {
        super(context, R.layout.push_item, items);
        this.context = (MyActivity)context;
        this.items = items;
        this.filteredItems = items;
    }

    @Override
    public int getCount() {

        return filteredItems.size();
    }

    public void refresh(ArrayList<PushItem> items)
    {
        this.items = items;
        this.filteredItems = items;
        notifyDataSetChanged();
    }

    @Override
    public PushItem getItem(int i) {
        return filteredItems.get(i);
    }


    public int getItemIndex(PushItem item) {
        return items.indexOf(item);
    }


    @Override
    public long getItemId(int i) {
        return i;
    }

    public String getDescription(String jsonDescription) {
        String description = "";
        if (!ApplicationParameters.simpleText) {
            if (ApplicationParameters.chosenTableFields.size() == 0) {
                return description;
            }
            try {
                // String json = new String(new String(jsonDescription.getBytes("ISO-8859-1"), "windows-1251").getBytes(), "UTF-8");
                JSONObject jObj = new JSONObject(jsonDescription);
                try {
                    description = "<b>" + ApplicationParameters.getTableFieldNameByKey(ApplicationParameters.chosenTableFields.get(0).getName(), ApplicationParameters.tableFields) + "</b>: ";
                    if (ApplicationParameters.chosenTableFields.get(0).getType().equals("date"))
                        description += dateFromTimestamp(jObj.getString(ApplicationParameters.chosenTableFields.get(0).getName()),1000) + "\n<br>";
                    else if (ApplicationParameters.chosenTableFields.get(0).getType().equals("bool"))
                        description += (Integer.parseInt(jObj.getString(ApplicationParameters.chosenTableFields.get(0).getName())) == 0 ? "Нет" : "Да") + "\n<br>";
                    else
                        description += jObj.getString(ApplicationParameters.chosenTableFields.get(0).getName()) + "\n<br>";
                } catch (Exception e) {
                    e.printStackTrace();
                }

                for (int i = 1; i < ApplicationParameters.chosenTableFields.size(); i++) {
                    try {

                        if (ApplicationParameters.chosenTableFields.get(i).getType().equals("date"))
                            description += "<b>" + ApplicationParameters.getTableFieldNameByKey(ApplicationParameters.chosenTableFields.get(i).getName(), ApplicationParameters.tableFields) + "</b>: " + dateFromTimestamp(jObj.getString(ApplicationParameters.chosenTableFields.get(i).getName()), 1000) + "\n<br>";
                        else if (ApplicationParameters.chosenTableFields.get(i).getType().equals("bool"))
                            description += "<b>" + ApplicationParameters.getTableFieldNameByKey(ApplicationParameters.chosenTableFields.get(i).getName(), ApplicationParameters.tableFields) + "</b>: " + (Integer.parseInt(jObj.getString(ApplicationParameters.chosenTableFields.get(i).getName())) == 0 ? "Нет" : "Да") + "\n<br>";
                        else
                            description += "<b>" + ApplicationParameters.getTableFieldNameByKey(ApplicationParameters.chosenTableFields.get(i).getName(), ApplicationParameters.tableFields) + "</b>: " + jObj.getString(ApplicationParameters.chosenTableFields.get(i).getName()) + "\n<br>";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                // } catch (UnsupportedEncodingException e) {
                //    e.printStackTrace();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else description = jsonDescription;
        return description;
    }

    public String modifyDescriptionCanavara(String desc) {
        String [] strs = desc.split("<br />");
        String result = "";
        for (String str:strs) {
            String [] local_strs = str.split(":");
            str = str.replaceFirst(local_strs[0], "<b>" + local_strs[0] + "</b>");
            result += str + "<br />";
        }
        return result;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        SharedPreferences unread = context.getSharedPreferences("UserPreference", 0);
        SharedPreferences.Editor prefsEditor = unread.edit();


        prefsEditor.putInt("unread", 0);
        prefsEditor.commit();
        LayoutInflater inflater = context.getLayoutInflater();
        if (view == null) {
            view = inflater.inflate(R.layout.push_item, viewGroup, false);
        }
        PushItem p = filteredItems.get(i);
        Spanned datetext = Html.fromHtml("<b>" + timeFromTimestamp(p.getDate(), 1) + "</b> " + dateFromTimestamp(p.getDate(), 1));
        ((TextView) view.findViewById(R.id.datetime)).setText(datetext);
       // ((TextView) view.findViewById(R.id.title)).setText(p.getTitle());
        Spanned text = Html.fromHtml((p != null && p.getType() != null && ApplicationParameters.isCanavara && !p.getType().equals("3")) ? modifyDescriptionCanavara(getDescription(parseText(p.getDescription()))) : getDescription(parseText(p.getDescription())));
        ((TextView) view.findViewById(R.id.description)).setText(text);
        view.setLongClickable(true);
        view.setOnCreateContextMenuListener(null);
        return view;
    }

    private String parseText(String input) {
        String regex = "(\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}):\\d{2}.\\d{4}";
        String output = input.replaceAll(regex, "$1");
        return output;
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
