package com.logistic.paperrose.mttp.oldversion.newsearch;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.TextView;

import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.dragndrop.DragNDropListView;
import com.logistic.paperrose.mttp.oldversion.search.DelayedFilteringField;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;
import com.logistic.paperrose.mttp.oldversion.settings.ChosenFieldsAdapterVer2;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

/**
 * Created by paperrose on 28.01.2015.
 */
public class DecFiltersAdapter extends ChosenFieldsAdapterVer2 {
    public DecFiltersAdapter(Context context, int textViewResourceId, ArrayList<TripleTableField> objects, DragNDropListView lv) {
        super(context, textViewResourceId, objects, lv, ApplicationParameters.chosenDeclarationSearchFields);

    }

    public HashMap<String, String> curFilters = new HashMap<String, String>();
    public HashMap<String, String> fieldFilters = new HashMap<String, String>();


    class MDatePickerDialog extends DatePickerDialog {
        public String dialogTitle = "";
        public MDatePickerDialog(Context context, OnDateSetListener callBack,
                                int year, int monthOfYear, int dayOfMonth, String title, OnDismissListener listener) {
            super(context, callBack, year, monthOfYear, dayOfMonth);
            setOnDismissListener(listener);
            dialogTitle = title;
            updateTitle();
        }

        public MDatePickerDialog(Context context, OnDateSetListener callBack,
                                 int year, int monthOfYear, int dayOfMonth, String title) {
            super(context, callBack, year, monthOfYear, dayOfMonth);
            dialogTitle = title;
            updateTitle();
        }

        @Override
        public void onDateChanged(DatePicker view, int year,
                                  int month, int day) {
            super.onDateChanged(view, year, month, day);
            updateTitle();
        }
        private void updateTitle() {
            setTitle(dialogTitle);
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        //все нахрен поменять как в ClientsAdapter
        //то же самое с Chosen-ами
        //convertView = super.getView(position, convertView, parent);
        View view = convertView;
        if (view == null  || views.size() < chosenList.size()) {
            view = lInflater.inflate(R.layout.filter_list_layout, parent, false);
            final CheckBox tw = (CheckBox)view.findViewById(R.id.field);
            TextView tv = (TextView)view.findViewById(R.id.fieldText);
            final TextView et  = (TextView)view.findViewById(R.id.fieldVal);

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
                        firstListAdapter.saveChanges(ctx.getSharedPreferences("UserPreference", 0));
                        fieldFilters.clear();
                        notifyDataSetChanged();
                    } catch (IndexOutOfBoundsException e) {

                    }
                }
            });
            final Calendar myCalendar = Calendar.getInstance();

            final DatePickerDialog.OnDateSetListener date2 = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    // TODO Auto-generated method stub
                    myCalendar.set(Calendar.YEAR, year);
                    myCalendar.set(Calendar.MONTH, monthOfYear);
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    String myFormat = "dd.MM.yyyy"; //In which you need put here
                    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                    if (!et.getText().equals(sdf.format(myCalendar.getTime()))) {
                        et.setText(fieldFilters.get(getItem(position).getName()) + " - " + sdf.format(myCalendar.getTime()));
                        fieldFilters.put(getItem(position).getName() + "___2", sdf.format(myCalendar.getTime()));
                        curFilters.put(getItem(position).getName() + "___2", sdf.format(myCalendar.getTime()));
                    }
                }

            };

            final DatePickerDialog.OnDismissListener disPP = new DatePickerDialog.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    new MDatePickerDialog(ctx, date2, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH), "Искать до").show();
                }
            };
            final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    // TODO Auto-generated method stub
                    myCalendar.set(Calendar.YEAR, year);
                    myCalendar.set(Calendar.MONTH, monthOfYear);
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    String myFormat = "dd.MM.yyyy"; //In which you need put here
                    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                    et.setText(sdf.format(myCalendar.getTime()));
                    fieldFilters.put(getItem(position).getName(), sdf.format(myCalendar.getTime()));
                    curFilters.put(getItem(position).getName(), sdf.format(myCalendar.getTime()));

                }

            };




            et.setClickable(true);
            et.setFocusable(false);
            if (p.getType().equals("date")) {

                et.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        new MDatePickerDialog(ctx, date, myCalendar
                                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                                myCalendar.get(Calendar.DAY_OF_MONTH), "Искать от", disPP).show();
                    }
                });
            } else {


                et.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        final DelayedFilteringField input = new DelayedFilteringField(ctx);
                        final TripleTableField[] book = new TripleTableField[1];
                        if (getItem(position).getType().equals("dict")) {
                            input.setThreshold(1);
                            input.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                                    book[0] = (TripleTableField) adapterView.getItemAtPosition(position);
                                    input.setText(book[0].getText());
                                }
                            });

                            DictionaryAutoCompleteAdapter autoCompleteAdapter;
                            autoCompleteAdapter = new DictionaryAutoCompleteAdapter(ctx, ApplicationParameters.getTableFieldByKey(getItem(position).getName(), ApplicationParameters.declarationDictionaries));
                            input.setAdapter(autoCompleteAdapter);
                        };
                        input.setText(et.getText());

                        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View view, boolean b) {
                                if (b) {

                                    input.showDropDown();
                                }
                            }
                        });
                        // TODO Auto-generated method stub
                        AlertDialog.Builder dial = new AlertDialog.Builder(ctx);
                        dial.setTitle(getItem(position).getText());
                        dial.setMessage("Введите значение фильтра");
                        dial.setView(input);
                        dial.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        try {
                                            Editable value = input.getText();
                                            et.setText(input.getText());
                                            fieldFilters.put(getItem(position).getName(), value.toString());
                                            curFilters.put(getItem(position).getName(), value.toString());
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
                });
            }

            et.setHint(p.getText());
            try {

                JSONArray filters = ApplicationParameters.decLastSearchString.getJSONArray("filters");
                for (int i = 0; i < filters.length(); i++) {
                    if (p.getName().equals(filters.getJSONObject(i).getString("field_name"))) {
                        et.setText(filters.getJSONObject(i).getString("field_value"));
                        fieldFilters.put(p.getName(), filters.getJSONObject(i).getString("field_value"));
                        curFilters.put(p.getName(), filters.getJSONObject(i).getString("field_value"));
                        if (p.getType().equals("date") && filters.getJSONObject(i).getString("field_value2") != null) {

                            fieldFilters.put(p.getName()+"___2", filters.getJSONObject(i).getString("field_value2"));
                            curFilters.put(p.getName()+"___2", filters.getJSONObject(i).getString("field_value2"));
                            et.setText(filters.getJSONObject(i).getString("field_value") + " - " + filters.getJSONObject(i).getString("field_value2"));
                        }
                    }
                }



            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {

            }
            Iterator<String> keys = curFilters.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                if (p.getName().equals(key)) {

                    et.setText(curFilters.get(key));
                    if (p.getType().equals("date") && curFilters.get(key+"___2") != null) {
                        et.setText(curFilters.get(key) + " - " + curFilters.get(key + "___2"));
                        fieldFilters.put(key + "___2", curFilters.get(key + "___2"));
                    }
                    fieldFilters.put(p.getName(), curFilters.get(key));
                }
            }
            views.add( view);
        } else {
            view = views.get(position);
        }
        int colorPos = position % ApplicationParameters.colors.length;
        view.setBackgroundColor(ApplicationParameters.colors[colorPos]);
        // holder.cb.setChecked(true);
        return view;
    }
}
