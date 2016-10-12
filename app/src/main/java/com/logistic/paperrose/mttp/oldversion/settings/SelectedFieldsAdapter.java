package com.logistic.paperrose.mttp.oldversion.settings;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.dragndrop.DropListener;
import com.logistic.paperrose.mttp.oldversion.dragndrop.RemoveListener;
import com.logistic.paperrose.mttp.oldversion.search.DelayedFilteringField;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.utils.ColoredArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by paperrose on 16.03.2015.
 */
public class SelectedFieldsAdapter extends ColoredArrayAdapter<TripleTableField> implements RemoveListener, DropListener {
    ArrayList<TripleTableField> selectedFields = new ArrayList<TripleTableField>();
    //BaseLogisticActivity activity = null;
    public SimpleFieldsAdapter simpleFieldsAdapter;
    Context ctx;
    LayoutInflater lInflater;
    public SelectedFieldsAdapter(Context context, int textViewResourceId, ArrayList<TripleTableField> objects) {
        super(context, textViewResourceId, objects);
        selectedFields = objects;
        ctx = context;
        lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void onDrop(int from, int to) {
        TripleTableField temp =  selectedFields.get(from);
        selectedFields.remove(from);
        selectedFields.add(to, temp);
        notifyDataSetChanged();
    }

    @Override
    public void onRemove(int which) {
        if (which < 0 || which > selectedFields.size()-1) return;
        selectedFields.remove(which);
        notifyDataSetChanged();
    }

    class SelectedFieldHolder {
        ImageView img;
        TextView name;
        TextView val;
        CheckBox cb;
    }

    @Override
    public int getCount() {
        return selectedFields.size();
    }

    // элемент по позиции
    @Override
    public TripleTableField getItem(int position) {
        return selectedFields.get(position);
    }


    public HashMap<String, String> fieldFilters = new HashMap<String, String>();
    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SelectedFieldHolder holder = null;

        final TripleTableField p = getItem(position);
        if (convertView == null) {
            convertView = lInflater.inflate(R.layout.filter_list_layout, null);
            holder = new SelectedFieldHolder();
            holder.cb = (CheckBox) convertView.findViewById(R.id.isChecked);
            holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    int getPosition = (Integer) compoundButton.getTag();
                    fieldFilters.clear();
                    simpleFieldsAdapter.addItem(getItem(getPosition));
                }
            });
            holder.img = (ImageView)convertView.findViewById(R.id.upItem);
            holder.name = (TextView)convertView.findViewById(R.id.fieldText);
            holder.val = (TextView)convertView.findViewById(R.id.fieldVal);
            holder.name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view2) {
                    if (p.getType().equals("date")) {
                        final Calendar myCalendar = Calendar.getInstance();
                        new DatePickerDialog(ctx, new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                                  int dayOfMonth) {
                                // TODO Auto-generated method stub
                                myCalendar.set(Calendar.YEAR, year);
                                myCalendar.set(Calendar.MONTH, monthOfYear);
                                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                String myFormat = "dd.MM.yyyy"; //In which you need put here
                                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                                ((TextView)view2).setText(sdf.format(myCalendar.getTime()));
                            }

                        }, myCalendar
                                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                    } else {
                        final DelayedFilteringField input = new DelayedFilteringField(ctx);

                        input.setText(((TextView)view2).getText());
                        // TODO Auto-generated method stub
                        AlertDialog.Builder dial = new AlertDialog.Builder(ctx);
                        dial.setTitle("Фильтр");
                        dial.setMessage("Введите значение фильтра");
                        dial.setView(input);
                        dial.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                try {
                                    Editable value = input.getText();
                                    ((TextView)view2).setText(input.getText());
                                    fieldFilters.put(getItem((Integer)((TextView)view2).getTag()).getName(), value.toString());

                                } catch (Exception e) {

                                }

                            }
                        }).setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        });
                        try {
                            dial.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            convertView.setTag(holder);
            convertView.setTag(R.id.field, holder.cb);
            convertView.setTag(R.id.upItem, holder.img);
            convertView.setTag(R.id.fieldText, holder.name);
            convertView.setTag(R.id.fieldVal, holder.val);
            holder.name.setText(p.getText());
            try {

                JSONArray filters = ApplicationParameters.lastSearchString.getJSONArray("filters");
                for (int i = 0; i < filters.length(); i++) {
                    if (p.getName().equals(filters.getJSONObject(i).getString("field_name"))) {
                        holder.val.setText(filters.getJSONObject(i).getString("field_value"));
                        fieldFilters.put(p.getName(), filters.getJSONObject(i).getString("field_value"));
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {

            }

        } else {
            holder = (SelectedFieldHolder) convertView.getTag();
        }

        holder.cb.setTag(position);
        holder.img.setTag(position);
        holder.name.setTag(position);
        holder.val.setTag(position);
        holder.val.setClickable(true);
        holder.val.setFocusable(false);
        holder.cb.setChecked(false);
        int colorPos = position % ApplicationParameters.colors.length;
        convertView.setBackgroundColor(ApplicationParameters.colors[colorPos]);
      //  convertView = super.getView(position, convertView, parent);
        return convertView;
    }

}
