package com.logistic.paperrose.mttp.oldversion.reports;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.logistic.paperrose.mttp.oldversion.BaseLogisticActivity;
import com.logistic.paperrose.mttp.oldversion.LoginActivity;
import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;
import com.logistic.paperrose.mttp.oldversion.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupReportActivity extends BaseLogisticActivity {
    ArrayList<ReportItemNode> nodes;
    ReportTreeListViewAdapter adapter;
    ListView myDrawerList;
    String reportID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLayoutId(R.layout.activity_group_report);
        super.onCreate(savedInstanceState);
        reportID = "3";//Integer.toString(getIntent().getIntExtra("report_id", -1));
        new CheckToken().execute();
        showProgress(true);
       /* for (int i = 0; i < 10; i++) {
            ReportItemNode settingsNode = new ReportItemNode("Outer" + Integer.toString(i), Integer.toString(i), null, nodes, "bookmark_settings");
            for (int j = 0; j < 5; j++) {
                ReportItemNode node = new ReportItemNode("Inner" + Integer.toString(j), Integer.toString(i*j), settingsNode, nodes, "bookmark_settings");
            }
        }*/


    }

    public JSONObject checkServerCredentials(final String reportID) throws JSONException {
        final String access_token = getPref("access_token");
        final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
        JSONParser parser = new JSONParser();
        JSONObject obj = parser.getJSONFromUrl("http://"+ApplicationParameters.MAIN_DOMAIN +"/declarations/get_statistic", new HashMap<String, String>() {{
            put("access_token", access_token);
            put("hs", secure_token);
            put("report_id", reportID);
        }}, getApplicationContext());
        return obj;
    }

    private class CheckToken extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... args) {
            JSONObject jsonObject = null;
            if (ApplicationParameters.GCM_KEY.isEmpty()) return new JSONObject() {{
                try {
                    put("error", "err");
                    put("status_code", 401);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }};
            try {
                jsonObject = checkServerCredentials(reportID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            showProgress(false);
            if (res == null) {
                Toast.makeText(GroupReportActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
            } else
            if (res.has("error")) {
                try {
                    if (res.getString("error").equals("Results count is too big")) {
                        return;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (res.getInt("status_code") == 401) {
                        if (!ApplicationParameters.loginOpened) {
                            ApplicationParameters.loginOpened = true;
                            Intent intent = new Intent(GroupReportActivity.this, LoginActivity.class);
                            intent.putExtra("login", true);
                            startActivity(intent);
                            finish();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    saveCredentials(res.getString("access_token"), "access_token");
                    nodes = new ArrayList<ReportItemNode>();
                    JSONArray result = res.getJSONArray("res");
                    getLevel(result, null);

                    adapter = new ReportTreeListViewAdapter(GroupReportActivity.this, R.layout.tree_node_item, nodes);
                    myDrawerList = (ListView)findViewById(R.id.clientList);
                    myDrawerList.setAdapter(adapter);
                    myDrawerList.setOnItemClickListener(new DrawerItemClickListener());
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        protected void getLevel(JSONArray arr, ReportItemNode parent) throws JSONException {
            for (int i = 0; i < arr.length(); i++) {
                ReportItemNode settingsNode = new ReportItemNode(arr.getJSONObject(i).getString("name"), arr.getJSONObject(i).getString("count"), parent, nodes, arr.getJSONObject(i).getString("field_name"));
                settingsNode.setImageID(R.drawable.add_beautiful_small, R.drawable.remove_beautiful_small);
                getLevel(arr.getJSONObject(i).getJSONArray("records"), settingsNode);
            }
        }
    }


    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(
                AdapterView<?> parent, View view, int position, long id
        ) {
            ReportItemNode node = nodes.get(position);
            if (node.opened) {
                adapter.hideItemChildNodes(node);

            } else {
                for (int i = 0; i < nodes.size(); i++) {
                    if (nodes.get(i).opened && !adapter.hasNode(nodes.get(i), node)) {
                        adapter.hideItemChildNodes(nodes.get(i));
                    }
                }
                adapter.showItemChildNodes(node);

            }
            myDrawerList.setItemChecked(position, true);
            myDrawerList.setSelection(position);
            if (node.getChildNodes().size() == 0) {
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.group_report, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
