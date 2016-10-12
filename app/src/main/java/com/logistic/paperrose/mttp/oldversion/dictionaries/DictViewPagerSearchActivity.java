package com.logistic.paperrose.mttp.oldversion.dictionaries;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.logistic.paperrose.mttp.oldversion.BaseLogisticActivity;
import com.logistic.paperrose.mttp.oldversion.LoginActivity;
import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.newsearch.SearchPageAdapter;
import com.logistic.paperrose.mttp.oldversion.results.SearchResults;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;
import com.logistic.paperrose.mttp.oldversion.utils.ExtendedEditText;
import com.logistic.paperrose.mttp.oldversion.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

public class DictViewPagerSearchActivity extends BaseLogisticActivity {

    List<View> pages = new ArrayList<View>();
    ViewPager viewPager;
    LayoutInflater inflater;
    public JSONArray jsonResults;
    //public SearchResults parentActivity = null;
    public int lastNum;
    public int currentPageNum;
    public ArrayList<TripleTableField> singleFields = new ArrayList<TripleTableField>();
    DictSearchViewPagerAdapter pagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLayoutId(R.layout.dict_activity_view_pager_search);
        singleFields = ApplicationParameters.currentDictFields;
        super.onCreate(savedInstanceState);
        inflater = LayoutInflater.from(DictViewPagerSearchActivity.this);
        viewPager = (ViewPager)findViewById(R.id.viewPager);
        try {
            jsonResults = ApplicationParameters.tempResults;
            if (jsonResults == null) finish();
            else {
                for (int i = 0; i < jsonResults.length(); i++) {
                    createPage(jsonResults.getJSONObject(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        pagerAdapter = new DictSearchViewPagerAdapter(pages);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                if (adapters.get(currentPageNum).isChanged()) {
                    showDialog(1);
                } else {
                    currentPageNum = i;
                }

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        lastNum = getIntent().getIntExtra("page_num", 1);
        statusTest = getIntent().getStringExtra("statusTest");
        currentPageNum = lastNum;
        viewPager.setCurrentItem(lastNum);
       // parentActivity = (SearchResults)ApplicationParameters.activity;
        ExtendedEditText et = (ExtendedEditText) findViewById(R.id.search);
        et.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapters.get(currentPageNum).getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        Button findByRec = (Button)findViewById(R.id.actionSearch);
        findByRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress(true);
                (new CheckToken2()).execute(
                        ApplicationParameters.getTableFieldNameByKey(
                                ApplicationParameters.getTableFieldNameByKey(
                                        statusTest, ApplicationParameters.currentDictFields),
                                adapters.get(currentPageNum).objects)
                );
            }
        });
        Button saveToFile = (Button)findViewById(R.id.saveToFile);
        saveToFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Uri uri = generateNoteOnSD(statusTest + "__" + ApplicationParameters.getTableFieldByKey("Номер записи", adapters.get(currentPageNum).objects).getText() + ".txt", getPageBody());
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(sharingIntent, "Отправить"));

            }
        });
    }

    public String getPageBody() {
        return adapters.get(currentPageNum).getPageBody();
    }

    public void refresh(JSONArray results) {
        pages.clear();
        jsonResults = results;
        try {
            for (int i = 0; i < jsonResults.length(); i++) {
                createPage(jsonResults.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DictSearchViewPagerAdapter pagerAdapter = new DictSearchViewPagerAdapter(pages);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(lastNum);
        pagerAdapter.notifyDataSetChanged();
    }

    public void createPage(JSONObject obj) throws JSONException {
        View page = inflater.inflate(R.layout.dict_page_view, null);
        ListView lv = (ListView) page.findViewById(R.id.searchResult);
        Iterator<String> keys = obj.keys();
        ArrayList<TripleTableField> objS = new ArrayList<TripleTableField>();
        ArrayList<String> copy = new ArrayList<String>();
        while (keys.hasNext())
            copy.add(keys.next());
        for (int i = 0; i < singleFields.size(); i++) {
            for (int k = 0; k < copy.size(); k++) {
                String key = copy.get(k);
                if (singleFields.get(i).getName().equals(key) && !obj.getString(key).isEmpty()) {
                    objS.add(
                            new TripleTableField(
                                    ApplicationParameters.getTableFieldNameByKey(key, ApplicationParameters.currentDictFields),
                                    obj.getString(key),
                                    ApplicationParameters.getFieldType(key, ApplicationParameters.currentDictFields)
                            )
                    );
                }
            }
        }
        final SearchPageAdapter adapter = new SearchPageAdapter(DictViewPagerSearchActivity.this, R.layout.search_field_row, objS);
       // adapter.pageID = Integer.parseInt(obj.getString("RECORD_ID"));

        lv.setAdapter(adapter);
        registerForContextMenu(lv);
        adapters.add(adapter);

        pages.add(page);
    }
    public String statusTest = "LIST_CLIENT_NAME";
    public String getPref(String key) {
        return getSharedPreferences("UserPreference", 0).getString(key, "123");
    }

    ArrayList<SearchPageAdapter> adapters = new ArrayList<SearchPageAdapter>();

    private ShareActionProvider mShareActionProvider;


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.dict_view_pager_search, menu);
      //  MenuItem item = menu.findItem(R.id.menu_item_share);


        return true;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       /* mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareHistoryFileName("custom_share_history.xml");
            Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                    .setType("text/plain").addStream(
                            generateNoteOnSD(statusTest + "__" +
                                            ApplicationParameters.getTableFieldByKey("Номер записи", adapters.get(currentPageNum).objects).getText() + ".txt",
                                    getPageBody())

                    ).getIntent();
            // Set the share Intent
            mShareActionProvider.setShareIntent(shareIntent);
        }*/
        return true;
    }




    public Uri generateNoteOnSD(String sFileName, String sBody){
        try
        {
            File root = new File(Environment.getExternalStorageDirectory(), "Logistic/Dictionary Records/" + statusTest.toLowerCase());
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            Uri uri = Uri.fromFile(gpxfile);
            //shareIntent.putExtra(Intent.EXTRA_TEXT, sBody);
            return uri;
            //Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public String getDateFromTimestamp(String stamp) {
        if (stamp.isEmpty() || stamp == null) return "";
        long unixSeconds = new Long(stamp);
        Date date = new Date(unixSeconds*1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy"); // the format of your date
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+4")); // give a timezone reference for formating (see comment at the bottom
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

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


    public JSONObject checkServerCredentials(final String filter) throws JSONException {
        final String access_token = getPref("access_token");
        final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
        JSONParser parser = new JSONParser();
        JSONObject obj = parser.getJSONFromUrl("http://"+ApplicationParameters.MAIN_DOMAIN +"/search", new HashMap<String, String>() {{
            put("access_token", access_token);
            put("hs", secure_token);
            put("filter_object", getJSONParameters(filter));
        }}, DictViewPagerSearchActivity.this);
        return obj;
    }

    private class CheckToken2 extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... args) {
            JSONObject jsonObject = null;
            try {
                jsonObject = checkServerCredentials(args[0]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            if (res == null) {
                Toast.makeText(DictViewPagerSearchActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
            } else

            if (res.has("error")) {
                showProgress(false);
                try {
                    if (res.getString("error").equals("Results count is too big")) {
                        return;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(DictViewPagerSearchActivity.this, LoginActivity.class);
                intent.putExtra("login", true);
                startActivity(intent);
                finish();
            } else {
                try {
                    saveCredentials(res.getString("access_token"), "access_token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(DictViewPagerSearchActivity.this, SearchResults.class);
                try {
                    JSONArray arr2 = new JSONArray();
                    ApplicationParameters.lastResults = new JSONArray(res.getString("result"));
                    ApplicationParameters.tempResults = new JSONArray(res.getString("result"));
                    ApplicationParameters.currentLength = Integer.parseInt(res.getString("count"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
                finish();

            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info;

        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            return false;
        }

        switch (item.getItemId()) {
            case R.id.copy:
                copyToClipboard(adapters.get(currentPageNum).getItem(info.position).getText());
                break;
            case R.id.cancel: closeContextMenu(); break;
            default: break;
        }
        return true;
    }

    public void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", text);
        clipboard.setPrimaryClip(clip);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pushes_context_menu, menu);
    }



}
