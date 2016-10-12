package com.logistic.paperrose.mttp.oldversion.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.logistic.paperrose.mttp.oldversion.BaseLogisticActivity;
import com.logistic.paperrose.mttp.oldversion.LoginActivity;
import com.logistic.paperrose.mttp.oldversion.MainActivity;
import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.clients.SettingsType;
import com.logistic.paperrose.mttp.oldversion.dragndrop.DragListener;
import com.logistic.paperrose.mttp.oldversion.dragndrop.DragNDropListView;
import com.logistic.paperrose.mttp.oldversion.dragndrop.DropListener;
import com.logistic.paperrose.mttp.oldversion.dragndrop.RemoveListener;
import com.logistic.paperrose.mttp.oldversion.list.BaseChosenListAdapter;
import com.logistic.paperrose.mttp.oldversion.list.FieldsListAdapter;
import com.logistic.paperrose.mttp.oldversion.list.SettingsChosenListAdapter;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.utils.ExtendedEditText;
import com.logistic.paperrose.mttp.oldversion.utils.JSONParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class BookmarkSettingsActivity extends BaseLogisticActivity {
    int bookmarkID = 0;
    SettingsType type;
    int bookmarkType = 0;
    BaseChosenListAdapter adapter;
    FieldsListAdapter alertAdapter;
    int statusID = 0;
    ArrayList<TripleTableField> allFields = new ArrayList<TripleTableField>();
    ArrayList<TripleTableField> chosenFields = new ArrayList<TripleTableField>();
    ArrayList<TripleTableField> tempFields;
    ListView lvMain2;
    ListView lvMain;

    public boolean compareLists(ArrayList<TripleTableField> list1, ArrayList<TripleTableField> list2) {
        if (list1.size() != list2.size()) return true;
        for (int i = 0; i < list1.size(); i++) {
            if (!(list1.get(i).getName().equals(list2.get(i).getName()))) return true;
        }
        return false;
    }

    public boolean checkChanges() {
        return compareLists(tempFields, chosenFields);
    }

    @Override
    public void onBackPressed() {
        if (checkChanges()) {
            showDialog(1);
        } else {
            super.onBackPressed();
        }
    }

    public String getPref(String key) {
        return getSharedPreferences("UserPreference", 0).getString(key, "123");
    }

    protected class CheckToken extends AsyncTask<Pair<String, HashMap<String, String>>, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Pair<String, HashMap<String, String>>... args) {
            JSONObject jsonObject = null;
            try {
                jsonObject = checkServerCredentials();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            try {
                if (res == null) {
                    Toast.makeText(BookmarkSettingsActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
                } else
                if (res.has("error")) {
                    if (res.getInt("status_code") == 401) {
                        Intent intent = new Intent(BookmarkSettingsActivity.this, LoginActivity.class);
                        intent.putExtra("login", true);
                        startActivity(intent);
                        finish();
                    } else {
                        showProgress(false);
                        Toast.makeText(getApplicationContext(), res.getString("error"), Toast.LENGTH_LONG).show();
                    }
                } else {
                    saveCredentials(res.getString("access_token"), "access_token");
                    JSONObject obj = res.getJSONObject("bookmark_id");
                    ApplicationParameters.customBookmarks.put(statusID, new Pair<Integer, String>(Integer.parseInt(obj.getString("ID")), obj.getString("FIELDS_ORDER")));
                    if (ApplicationParameters.activity != null) {
                        MainActivity.saveChanges(getApplicationContext().getSharedPreferences("UserPreference", 0));
                    }
                    ApplicationParameters.changed = true;
                    //
                    finish();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    protected Dialog onCreateDialog(int id) {
        if (id == 1) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            // заголовок
            adb.setTitle("Предупреждение");
            // сообщение
            adb.setMessage("Сохранить изменения перед выходом?");
            // иконка
            adb.setIcon(android.R.drawable.ic_dialog_info);
            // кнопка положительного ответа
            adb.setPositiveButton("Сохранить", myClickListener);
            adb.setNegativeButton("Не сохранять", myClickListener);
            // кнопка отрицательного ответа
            adb.setNeutralButton("Отмена", myClickListener);
            // кнопка нейтрального ответа
            // создаем диалог
            return adb.create();
        }
        return super.onCreateDialog(id);
    }
    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                // положительная кнопка
                case Dialog.BUTTON_POSITIVE:
                    showProgress(true);
                    (new CheckToken()).execute();
                    break;
                // нейтральная кнопка
                case Dialog.BUTTON_NEGATIVE:
                    finish();
                    break;
                case Dialog.BUTTON_NEUTRAL:
                    break;
            }
        }
    };

    public JSONObject checkServerCredentials() throws JSONException {
        final String access_token = getPref("access_token");
        final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
        JSONParser parser = new JSONParser();
        String req = "/edit_bookmark_order";
        if (bookmarkType == 1) req = "/user/custom_bookmark_settings";
        JSONObject obj = parser.getJSONFromUrl("http://"+ApplicationParameters.MAIN_DOMAIN +req, new HashMap<String, String>() {{
            put("access_token", access_token);
            put("hs", secure_token);
            String p1 = "";
            String finalOrder = "";
            if (chosenFields.size() > 0) {
                p1 += Integer.toString(chosenFields.get(0).id);
                for (int i = 1; i < chosenFields.size(); i++) {
                    p1 += ">" + Integer.toString(chosenFields.get(i).id);
                }
                if (type == SettingsType.SINGLE)
                    finalOrder = tOrder + "<<<" + p1;
                else if (type == SettingsType.TABLE)
                    finalOrder = p1 + "<<<" + sOrder;
            } else {
                finalOrder = getIntent().getStringExtra("order");
            }
            ApplicationParameters.setBookmarkOrderByID(bookmarkID, finalOrder);
            put("order", finalOrder);
            put("login", getPref("login"));
            put("bookmark_id", Integer.toString(bookmarkID));
            put("status_id", Integer.toString(statusID));
        }}, getApplicationContext());
        return obj;
    }
    String tOrder = "";
    String sOrder = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        type = SettingsType.values()[getIntent().getIntExtra("adapterType", 1)];
        bookmarkID = getIntent().getIntExtra("bookmarkID", 0);
        bookmarkType = getIntent().getIntExtra("bookmarkType", 0);
        statusID = getIntent().getIntExtra("statusID", 0);
        String order = getIntent().getStringExtra("order");
        if (!order.isEmpty()) {
            String[] orders = order.split("<<<");
            tOrder = orders[0];
            if (orders.length == 1) sOrder = "";
            else sOrder = orders[1];
        }
        switch (type) {
            case SINGLE:
                if (sOrder.isEmpty())
                    tempFields = new ArrayList<TripleTableField>(ApplicationParameters.chosenSearchResultSingleFields);
                else {
                    tempFields = new ArrayList<TripleTableField>();
                    String[] sOrders = sOrder.split(">");
                    for (int i = 0; i < sOrders.length; i++) {
                        if (ApplicationParameters.getTableFieldById(Integer.parseInt(sOrders[i]), ApplicationParameters.tableFields) != null)
                            tempFields.add(ApplicationParameters.getTableFieldById(Integer.parseInt(sOrders[i]), ApplicationParameters.tableFields));
                    }
                }

                break;
            case TABLE:
                if (tOrder.isEmpty())
                    tempFields = new ArrayList<TripleTableField>(ApplicationParameters.chosenSearchResultsTableFields);
                else {
                    tempFields = new ArrayList<TripleTableField>();
                    String[] tOrders = tOrder.split(">");
                    for (int i = 0; i < tOrders.length; i++) {
                        if (ApplicationParameters.getTableFieldById(Integer.parseInt(tOrders[i]), ApplicationParameters.tableFields) != null)
                            tempFields.add(ApplicationParameters.getTableFieldById(Integer.parseInt(tOrders[i]), ApplicationParameters.tableFields));
                    }

                }
                break;
            default: break;
        }
        setLayoutId(R.layout.activity_test);
        super.onCreate(savedInstanceState);
        chosenFields.addAll(tempFields);
        allFields.addAll(ApplicationParameters.tableFields);

        lvMain = (ListView)findViewById(R.id.testList);
        adapter = new SettingsChosenListAdapter(BookmarkSettingsActivity.this, R.layout.text_edit_list_layout_ver2, chosenFields);
        alertAdapter = new FieldsListAdapter(BookmarkSettingsActivity.this, R.layout.text_edit_list_layout_ver2, allFields);
        alertAdapter.setChosenAdapter(adapter);
        adapter.flAdapter = alertAdapter;

        lvMain.setAdapter(adapter);
        if (lvMain instanceof DragNDropListView) {
            ((DragNDropListView) lvMain).setDropListener(mDropListener);
            ((DragNDropListView) lvMain).setRemoveListener(mRemoveListener);
            ((DragNDropListView) lvMain).setDragListener(mDragListener);
        }
        Button alertButton = (Button)findViewById(R.id.addFields);
        alertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LinearLayout view2 = (LinearLayout) getLayoutInflater()
                        .inflate(R.layout.custom_dialog, null);

                lvMain2 = (ListView) view2.findViewById(R.id.alertFieldsList);
                lvMain2.setAdapter(alertAdapter);
                ExtendedEditText et = (ExtendedEditText) view2.findViewById(R.id.search);
                final CheckBox cb = (CheckBox) view2.findViewById(R.id.selectAll);
                if (alertAdapter.items.size() == adapter.items.size()) {
                    cb.setText("Удалить все");
                    cb.setChecked(true);
                }
                cb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cb.setChecked(!cb.isSelected());
                    }
                });
                cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (b) {
                            cb.setText("Удалить все");
                            adapter.items.clear();
                            adapter.items.addAll(alertAdapter.items);

                        } else {
                            cb.setText("Выбрать все");
                            adapter.items.clear();
                        }
                        alertAdapter.refresh();
                        alertAdapter.notifyDataSetChanged();
                        adapter.notifyDataSetChanged();
                    }
                });
                et.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        alertAdapter.getFilter().filter(s.toString());
                        alertAdapter.filterS = s;
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
                AlertDialog.Builder builder = new AlertDialog.Builder(BookmarkSettingsActivity.this);
                builder.setTitle("Добавление полей");
                builder.setMessage("Выберите поля");
                builder.setView(view2);
                Button okButton = (Button) view2.findViewById(R.id.okButton);
                final AlertDialog dialog = builder.create();
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            if (checkChanges()) {
                showDialog(1);
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private DropListener mDropListener =
            new DropListener() {
                public void onDrop(int from, int to) {
                    if (adapter instanceof SettingsChosenListAdapter) {
                        ((SettingsChosenListAdapter)adapter).onDrop(from, to);
                        lvMain.invalidateViews();
                        adapter.notifyDataSetChanged();

                    }
                }
            };

    private RemoveListener mRemoveListener =
            new RemoveListener() {
                public void onRemove(int which) {
                    if (adapter instanceof SettingsChosenListAdapter) {
                        ((SettingsChosenListAdapter)adapter).onRemove(which);
                        lvMain.invalidateViews();
                    }
                }
            };

    private DragListener mDragListener =
            new DragListener() {

                int backgroundColor = 0x44FF1C00;
                int defaultBackgroundColor;

                public void onDrag(int x, int y, ListView listView) {
                    // TODO Auto-generated method stub
                }

                public void onStartDrag(View itemView) {
                    itemView.setVisibility(View.GONE);
                    defaultBackgroundColor = itemView.getDrawingCacheBackgroundColor();
                    itemView.setBackgroundColor(backgroundColor);
                    ImageView iv = (ImageView)itemView.findViewById(R.id.upItem);
                    if (iv != null) iv.setVisibility(View.VISIBLE);
                }

                public void onStopDrag(View itemView) {
                    itemView.setVisibility(View.VISIBLE);
                    itemView.setBackgroundColor(defaultBackgroundColor);
                    ImageView iv = (ImageView)itemView.findViewById(R.id.upItem);
                    if (iv != null) iv.setVisibility(View.VISIBLE);
                }

            };

}
