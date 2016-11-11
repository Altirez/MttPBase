/*базовый класс для всех активити
* в метод setLayoutId передается необходимая xml-ина с внешним описанием.
* она является внутренним контейнером элементов и загружается во ViewStub (внешний хранит action bar и нижние кнопки)*/

package com.logistic.paperrose.mttp.oldversion;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.clients.ClientsActivity;
import com.logistic.paperrose.mttp.oldversion.contacts.ContactsActivity;
import com.logistic.paperrose.mttp.oldversion.menu.ItemNode;
import com.logistic.paperrose.mttp.oldversion.menu.TreeListViewAdapter;
import com.logistic.paperrose.mttp.oldversion.newsearch.BaseNewSearchActivity;
import com.logistic.paperrose.mttp.oldversion.newsearch.NewSearchActivity;
import com.logistic.paperrose.mttp.oldversion.pushes.MyActivity;
import com.logistic.paperrose.mttp.oldversion.reports.GroupReportActivity;
import com.logistic.paperrose.mttp.oldversion.results.BaseReportsSearchResults;
import com.logistic.paperrose.mttp.oldversion.results.BaseReportsViewPagerSearchActivity;
import com.logistic.paperrose.mttp.oldversion.results.BaseSearchResults;
import com.logistic.paperrose.mttp.oldversion.results.BaseViewPagerSearchActivity;
import com.logistic.paperrose.mttp.oldversion.results.SearchResults;
import com.logistic.paperrose.mttp.oldversion.results.ViewPagerSearchActivity;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;
import com.logistic.paperrose.mttp.oldversion.settings.Bookmark;
import com.logistic.paperrose.mttp.oldversion.settings.BookmarkSettingsActivity;
import com.logistic.paperrose.mttp.oldversion.settings.Report;
import com.logistic.paperrose.mttp.oldversion.settings.TableEntity;
import com.logistic.paperrose.mttp.oldversion.utils.JSONParser;
import com.logistic.paperrose.mttp.oldversion.utils.ScreenReceiver;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

//GOD OBJECT 1
public class BaseLogisticActivity extends ActionBarActivity {
    public ImageButton bt4;
    public ImageButton bt3;

    //отслеживаем все ошибки без Exception и отправляем их на сервер, а далее - в телеграм
    public class TryMe implements Thread.UncaughtExceptionHandler {

        Thread.UncaughtExceptionHandler oldHandler;

        public TryMe() {
            oldHandler = Thread.getDefaultUncaughtExceptionHandler(); // сохраним ранее установленный обработчик
        }

        @Override
        public void uncaughtException(Thread thread, final Throwable throwable) {

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    final StringWriter errors = new StringWriter();
                    throwable.printStackTrace(new PrintWriter(errors));
                    JSONParser parser = new JSONParser();
                    parser.getJSONFromUrl("http://service.g-soft.ru/error/new", new HashMap<String, String>() {{
                        put("error", errors.toString());
                        put("login", getPref("login"));
                    }}, getApplicationContext());
                    return null;
                }

                @Override
                protected void onPostExecute(Void msg) {
                    ;
                }
            }.execute(null, null, null);


            if (oldHandler != null)

                oldHandler.uncaughtException(thread, throwable);
        }
    }


    public boolean isFinishAfterClick() {
        return finishAfterClick;
    }

    public void setFinishAfterClick(boolean finishAfterClick) {
        this.finishAfterClick = finishAfterClick;
    }

    public int getLayoutId() {
        return layoutId;
    }

    //окна свойств несколько различаются запросом сохранения в базу, 3 варианта - свойства закладок, свойства закладок по умолчанию и общие свойства.
    public void startSettings() {
        ApplicationParameters.activity = this;
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("adapterType", 0);
        startActivity(intent);
    }

    public void startSettings(int adapterType) {
        ApplicationParameters.activity = this;
        Intent intent = new Intent(BaseLogisticActivity.this, SettingsActivity.class);
        intent.putExtra("adapterType", adapterType);
        startActivity(intent);
    }

    public void startSettings(int adapterType, int bookmarkID) {
        ApplicationParameters.activity = this;
        Intent intent = new Intent(BaseLogisticActivity.this, BookmarkSettingsActivity.class);
        intent.putExtra("bookmarkID", bookmarkID);
        intent.putExtra("order", ApplicationParameters.getBookmarkOrderByID(bookmarkID));
        intent.putExtra("adapterType", adapterType);
        intent.putExtra("bookmarkType", 0);
        startActivity(intent);
    }

    public void startSettings(int adapterType, int bookmarkID, boolean isCustom) {
        ApplicationParameters.activity = this;
        Intent intent = new Intent(BaseLogisticActivity.this, BookmarkSettingsActivity.class);
        intent.putExtra("statusID", bookmarkID);
        intent.putExtra("order", ApplicationParameters.getCustomBookmarkOrderByID(bookmarkID));
        intent.putExtra("adapterType", adapterType);
        intent.putExtra("bookmarkType", 1);
        startActivity(intent);
    }


    public boolean checked = false;

    @Override
    public void onPause() {
        super.onPause();
        //unregisterReceiver(mReceiver);

    }

    @Override
    public void onStop() {
        super.onStop();
    }


    LinearLayout ll;

    @Override
    public void onResume() {
        super.onResume();


        TextView tv = (TextView) BaseLogisticActivity.this.findViewById(R.id.counter);
        SharedPreferences unread = getSharedPreferences("UserPreference", 0);
        int unread_count = unread.getInt("unread", 0);
        tv.setText(Integer.toString(unread_count));
        tv.setVisibility(unread_count == 0 ? View.GONE : View.VISIBLE);
        setDrawerMenu();

        // THIS IS WHEN ONRESUME() IS CALLED WHEN THE SCREEN STATE HAS NOT CHANGED


        //registerReceiver(mReceiver, filter);
    }

    //основная функция, которая отвечает за повторную загрузку результатов поиска и т.д.
    public void refreshRequest() {

    }

    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }

    private int layoutId = 0;
    private boolean finishAfterClick = true;
    private ListView myDrawerList;
    private NavigationView myDrawerLayer;
    TreeListViewAdapter adapter;
    public ArrayList<ItemNode> nodes = new ArrayList<ItemNode>();
    int drawableID = R.drawable.search_results_white;

    RelativeLayout mainLayout;

    private float lastTranslate = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new TryMe());
        super.onCreate(savedInstanceState);
        if (!(BaseLogisticActivity.this instanceof MainActivity)) {
            setupActionBarWithText(BaseLogisticActivity.this.getTitle().toString());
        } else {
            setupActionBar();
        }
        setContentView(R.layout.activity_base_logistic);


        ll = (LinearLayout) findViewById(R.id.hiddenScreen);
        setTestMenu();
        setContent();
        setParams();
        setReceiver();
        setAdditionalParams();
        mainLayout = (RelativeLayout) findViewById(R.id.main_content);
        myDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (!(BaseLogisticActivity.this instanceof MainActivity)) {
            setupActionBarWithText(BaseLogisticActivity.this.getTitle().toString());

        } else {
            setupActionBar();
        }
        myDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View view, float v) {
                if (BaseLogisticActivity.this instanceof SettingsActivity && ((SettingsActivity) BaseLogisticActivity.this).checkChanges()) {
//                    myDrawerLayout.closeDrawer(myDrawerList);
                }
                // float moveFactor = (myDrawerLayer.() * v);

                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                {

                    mainLayout.setScaleY(Math.max((myDrawerList.getWidth()-moveFactor)/myDrawerList.getWidth(),0.7f));
                    mainLayout.setScaleX(Math.max((myDrawerList.getWidth()-moveFactor)/myDrawerList.getWidth(),0.7f));
                    mainLayout.setTranslationX(moveFactor);
                }
                else
                {
                    TranslateAnimation anim = new TranslateAnimation(lastTranslate, moveFactor, 0.0f, 0.0f);

                    anim.setDuration(0);
                    anim.setFillAfter(true);
                    mainLayout.startAnimation(anim);

                    lastTranslate = moveFactor;
                }*/
            }

            @Override
            public void onDrawerOpened(View view) {

            }

            @Override
            public void onDrawerClosed(View view) {

            }

            @Override
            public void onDrawerStateChanged(int i) {

            }
        });
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        //filter.addAction(Intent.ACTION_SCREEN_OFF);
        //mReceiver = new ScreenReceiver(); //не используется
        //registerReceiver(mReceiver, filter);
        //View view = this.getCurrentFocus();
        //if (view != null) {
            //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            //imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        //}
        // YOUR CODE
    }

    IntentFilter filter;
    public BroadcastReceiver mReceiver;


    public void setupActionBar() {
        //final ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(
        //          R.layout.action_bar_layout,
        //          null);
        try {
            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
        } catch (Exception e) {

        }
        try {
            ActionBar ab = getActionBar();
            ab.hide();
            this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        } catch (Exception e) {

        }
        // actionBar.setDisplayShowTitleEnabled(false);
        // actionBar.setDisplayShowHomeEnabled(false);
        // actionBar.setDisplayShowCustomEnabled(true);
        //   android.support.v7.app.ActionBar.LayoutParams layoutParams = new  android.support.v7.app.ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
        //           ActionBar.LayoutParams.MATCH_PARENT);
        //   layoutParams.gravity = Gravity.CENTER_HORIZONTAL|Gravity.CENTER_HORIZONTAL;
        //   actionBar.setCustomView(actionBarLayout,layoutParams);
        try {
            findViewById(R.id.starterAB).setVisibility(View.VISIBLE);
            findViewById(R.id.textAB).setVisibility(View.GONE);
            final LinearLayout actionBarLayout = (LinearLayout) findViewById(R.id.starterAB);
            ImageButton bt = (ImageButton) actionBarLayout.findViewById(R.id.menu_btn);
            bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (myDrawerLayout.isDrawerOpen(Gravity.RIGHT))
                        myDrawerLayout.closeDrawer(Gravity.RIGHT);
                    else
                        myDrawerLayout.openDrawer(Gravity.RIGHT);
                }
            });
        } catch (NullPointerException e) {

        }

       /* actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionBarLayout);*/
    }

    public void setupActionBarWithText(String text) {

        try {
            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
        } catch (Exception e) {

        }
        try {
            ActionBar ab = getActionBar();
            ab.hide();
            this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        } catch (Exception e) {

        }
        // actionBar.setDisplayShowTitleEnabled(false);
        // actionBar.setDisplayShowHomeEnabled(false);

        //actionBar.setDisplayShowCustomEnabled(true);
        //actionBar.setDisplayOptions(0, android.support.v7.app.ActionBar.DISPLAY_USE_LOGO);
        //  android.support.v7.app.ActionBar.LayoutParams layoutParams = new  android.support.v7.app.ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
        //          ActionBar.LayoutParams.MATCH_PARENT);
        //layoutParams.gravity = Gravity.CENTER_HORIZONTAL|Gravity.CENTER_HORIZONTAL;
        //actionBar.setCustomView(actionBarLayout, layoutParams);

        try {
            findViewById(R.id.starterAB).setVisibility(View.GONE);
            findViewById(R.id.textAB).setVisibility(View.VISIBLE);
            final LinearLayout actionBarLayout = (LinearLayout) findViewById(R.id.textAB);
            TextView tv = (TextView) actionBarLayout.findViewById(R.id.statusText);
            tv.setText(text);
            ImageButton bt = (ImageButton) actionBarLayout.findViewById(R.id.backButton);
            bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            ImageButton bt2 = (ImageButton) actionBarLayout.findViewById(R.id.menu_btn);
            bt2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (myDrawerLayout.isDrawerOpen(Gravity.RIGHT))
                        myDrawerLayout.closeDrawer(Gravity.RIGHT);
                    else
                        myDrawerLayout.openDrawer(Gravity.RIGHT);
                }
            });
            bt4 = (ImageButton) actionBarLayout.findViewById(R.id.save_btn);
            bt3 = (ImageButton) actionBarLayout.findViewById(R.id.fave_btn);
            if (BaseLogisticActivity.this instanceof NewSearchActivity) {
                bt3.setVisibility(View.VISIBLE);
            } else {
                bt3.setVisibility(View.GONE);
            }

            if (BaseLogisticActivity.this instanceof SettingsActivity) {
                bt4.setVisibility(View.VISIBLE);
            } else {
                bt4.setVisibility(View.GONE);
            }

        } catch (NullPointerException e) {

        }

       /* actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionBarLayout);*/
    }


    protected void setContent() {
        ViewStub stub = (ViewStub) findViewById(R.id.content);
        stub.setLayoutResource(layoutId);
        View inflated = stub.inflate();
    }

    protected void setReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("refresh_push_count"));
        //отвечает за счетчик непрочитанных пушей

    }

    private ActionBarDrawerToggle myDrawerToggle;
    private DrawerLayout myDrawerLayout; //боковая шторка

    protected void setParams() {
        mProgressView = findViewById(R.id.disableLayout);
        mProgressView.setAlpha(1);
        Button an0 = (Button) findViewById(R.id.angry_btn0); //нижняя кнопка home
        Button an1 = (Button) findViewById(R.id.angry_btn); //нижняя кнопка перехода к пушам
        //  Button an2 = (Button)findViewById(R.id.angry_btn2);
        Button an3 = (Button) findViewById(R.id.angry_btn3); //нижняя кнопка расширенного поиска
        an0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BaseLogisticActivity.this instanceof MainActivity) {
                    ((MainActivity) BaseLogisticActivity.this).setRootNodes();
                    return;
                }

                Intent intent = new Intent(BaseLogisticActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        an3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BaseLogisticActivity.this instanceof SettingsActivity && ((SettingsActivity) BaseLogisticActivity.this).checkChanges()) {
                    return;
                }
                if (BaseLogisticActivity.this instanceof NewSearchActivity) {
                    return;
                }
                if (ApplicationParameters.GCM_KEY.isEmpty()) {
                    if (!ApplicationParameters.loginOpened) {
                        ApplicationParameters.loginOpened = true;
                        Intent intent = new Intent(BaseLogisticActivity.this, LoginActivity.class);
                        intent.putExtra("login", true);
                        startActivity(intent);
                        finish();
                    }
                }
                Intent intent = new Intent(BaseLogisticActivity.this, NewSearchActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (finishAfterClick)
                    finish();

            }
        });
        an1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BaseLogisticActivity.this instanceof SettingsActivity && ((SettingsActivity) BaseLogisticActivity.this).checkChanges()) {
                    return;
                }
                if (BaseLogisticActivity.this instanceof MyActivity) {
                    return;
                }
                if (ApplicationParameters.GCM_KEY.isEmpty()) {
                    if (!ApplicationParameters.loginOpened) {
                        ApplicationParameters.loginOpened = true;
                        Intent intent = new Intent(BaseLogisticActivity.this, LoginActivity.class);
                        intent.putExtra("login", true);
                        startActivity(intent);
                        finish();
                    }
                }
                Intent intent = new Intent(BaseLogisticActivity.this, MyActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                if (finishAfterClick)
                    //BaseLogisticActivity.this.finish();
                finish();
            }
        });
        myDrawerLayer = (NavigationView) findViewById(R.id.left_drawer);

        myDrawerList = (ListView) findViewById(R.id.left_drawer_list);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        // int newWidth=4*width/5;
        // DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) myDrawerLayer.getLayoutParams();
        // params.width = newWidth;
        // myDrawerLayer.;
        adapter = new TreeListViewAdapter(this, R.layout.tree_node_item, nodes);
        myDrawerList.setAdapter(adapter);
        myDrawerList.setOnItemClickListener(new DrawerItemClickListener());


    }

    int maxn = 1;

    private void setEntity(TableEntity entity) {

    }

    public void saveBookmarks() {
        SharedPreferences historyPrefs = getSharedPreferences("UserPreference", 0);
        SharedPreferences.Editor prefsEditor = historyPrefs.edit();
        Gson gson = new Gson();
        String json2 = gson.toJson(ApplicationParameters.bookmarks);
        prefsEditor.putString("bookmarks", json2);
        prefsEditor.commit();
    }


    //устанавливает меню шторки
    public void setDrawerMenu() {
        loadBookmarks();
        nodes.clear();
        ItemNode refreshNode = new ItemNode(true, true, 0, "Обновить", null, R.drawable.nd_refresh, nodes, "refresh_request", R.drawable.refresh);

        ItemNode containerNode = new ItemNode(1, "Грузы", null, R.drawable.nd_containers, nodes, "containers", R.drawable.containers);
        setContainerStatuses(containerNode);

       /* if (ApplicationParameters.isCanavara)
        {
            ItemNode declarationNode = new ItemNode(6, "Отчеты проводящего", null, R.drawable.declarations, nodes, "decl2");
            setDeclarationStatuses(declarationNode);

            ItemNode declarationNum = new ItemNode(11, "Найти по номеру", declarationNode, R.drawable.search, nodes, "num_dec");
            ItemNode declarationSearch = new ItemNode(11, "Расширенный поиск", declarationNode, R.drawable.search, nodes, "search_dec");
        }*/

        ItemNode declarationNode2 = new ItemNode(6, "Декларации", null, R.drawable.nd_declarations, nodes, "declarations", R.drawable.declarations_back);
        //ItemNode declarationNode3 = new ItemNode(6, "Таможенные декларации", declarationNode2, R.drawable.nd_declarations, nodes, "declarations",-1);
        //ItemNode declarationTransit = new ItemNode(6, "Транзитные декларации", declarationNode2, R.drawable.nd_declarations, nodes, "declarations",-1);

        //ItemNode transitCurNode = new ItemNode(6, "Текущий месяц", declarationTransit, R.drawable.nd_declarations, nodes, "transit_cur", -1);
        //ItemNode transitArchNode = new ItemNode(6, "Архив", declarationTransit, R.drawable.nd_declarations, nodes, "transit_arch", -1);

        //if (ApplicationParameters.isCanavara)
          //  setDeclarationStatuses2(declarationNode3);
       // else
            setDeclarationStatuses(declarationNode2);


        //ItemNode declarationNum2 = new ItemNode(11, "Найти по номеру", declarationNode3, R.drawable.circle, nodes, "num_dec", -1);

        //ItemNode declarationSearch2 = new ItemNode(11, "Расширенный поиск", declarationNode3, R.drawable.circle, nodes, "search_dec", -1);

        if (ApplicationParameters.group_id != 2) {
            ItemNode dictNode = new ItemNode(6, "Справочники", null, R.drawable.nd_dicts, nodes, "dictionaries", R.drawable.dictionaries);
            ItemNode clientNode = new ItemNode(12, "Клиенты", dictNode, R.drawable.nd_clients, nodes, "clients", -1);
            ItemNode partnersNode = new ItemNode(13, "Партнеры", dictNode, R.drawable.nd_partners, nodes, "partners", -1);

            ItemNode parkNode = new ItemNode(12, "Перевозчики", dictNode, R.drawable.nd_drivers, nodes, "parks", -1);
            ItemNode terminalNode = new ItemNode(13, "Терминалы", dictNode, R.drawable.nd_terminals, nodes, "terminals", -1);

            ItemNode lineNode = new ItemNode(12, "Линии", dictNode, R.drawable.nd_lines, nodes, "lines", -1);
            ItemNode payersNode = new ItemNode(13, "Наши фирмы", dictNode, R.drawable.nd_firms, nodes, "payer", -1);
        }
        ItemNode bookmarksNode = new ItemNode(14, "Закладки", null, R.drawable.nd_bookmarks, nodes, "bookmarks", R.drawable.favorites);
        ItemNode addBookmarksNode = new ItemNode(14, "Создать новую", bookmarksNode, R.drawable.circle, nodes, "add_bookmark", -1);

        setBookmarks(15, bookmarksNode);
        ItemNode settingsNode = new ItemNode(false, true, 0, "Настройки", null, R.drawable.nd_settings, nodes, "settings", R.drawable.options);
        ItemNode settingsContNode = new ItemNode(false, true, 0, "Грузы", settingsNode, R.drawable.nd_containers_settings, nodes, "cont_settings", -1);
        ItemNode settingsTable = new ItemNode(0, "Вид таблицы", settingsContNode, R.drawable.nd_table_view, nodes, "table_settings", -1);
        ItemNode settingsSingle = new ItemNode(0, "Вид записи", settingsContNode, R.drawable.nd_single_view, nodes, "single_settings", -1);

       /* if (ApplicationParameters.isCanavara) {
            ItemNode settingsRepDecNode = new ItemNode(16+ApplicationParameters.bookmarks.size(), "Отчеты проводящего", settingsNode, R.drawable.settings_white, nodes, "dec_settings");
            ItemNode settingsRepDecTable = new ItemNode(16+ApplicationParameters.bookmarks.size(), "Вид таблицы", settingsRepDecNode, R.drawable.search_results_white, nodes, "rep_dec_table_settings");
            ItemNode settingsRepDecSingle = new ItemNode(17+ApplicationParameters.bookmarks.size(), "Вид записи", settingsRepDecNode, R.drawable.single_record_white, nodes, "rep_dec_single_settings");
        }*/

        ItemNode settingsDecNode = new ItemNode(false, true, 0, "Декларации", settingsNode, R.drawable.nd_declarations_settings, nodes, "dec_settings", -1);
        ItemNode settingsDecTable = new ItemNode(0, "Вид таблицы", settingsDecNode, R.drawable.nd_table_view, nodes, "dec_table_settings", -1);
        ItemNode settingsDecSingle = new ItemNode(0, "Вид записи", settingsDecNode, R.drawable.nd_single_view, nodes, "dec_single_settings", -1);
        createReports(nodes);
        ItemNode aboutNode = new ItemNode(true, true, 0, "Обратная связь", null, R.drawable.nd_review, nodes, "review_set", -1);
        ItemNode reviewNode = new ItemNode(true, true, 0, "Оставить отзыв", aboutNode, R.drawable.nd_review, nodes, "review", -1);
        ItemNode contactsNode = new ItemNode(true, true, 0, "О разработчике", aboutNode, R.drawable.nd_review, nodes, "contacts", -1);
        ItemNode authNode = new ItemNode(true, true, 0, "Выход из системы", null, R.drawable.nd_exit, nodes, "authorize", -1);

        adapter.notifyDataSetChanged();

    }

    //статусы контейнеров зависят от клиента
    public void setContainerStatuses(ItemNode parent) {
        if (ApplicationParameters.current_statuses != null) {
            //ТББ-шные
            try {
                for (int i = 0; i < ApplicationParameters.current_statuses.length(); i++) {
                    JSONObject obj = ApplicationParameters.current_statuses.getJSONObject(i);
                    ItemNode node = new ItemNode(i, obj.getString("main_status_name"), parent, R.drawable.circle, nodes, "main_cont_status", -1);
                    node.count = obj.getString("count");
                    JSONArray arr = obj.getJSONArray("statuses");
                    for (int j = 0; j < arr.length(); j++) {
                        JSONObject obj1 = arr.getJSONObject(j);
                        ItemNode nd = new ItemNode(i, obj1.getString("status_name"), node, R.drawable.circle, nodes, "cont_status", -1);
                        nd.count = obj1.getString("count");

                        ItemNode settingsNode = new ItemNode(i, "Настройки", nd, R.drawable.circle, nodes, "bookmark_settings", -1);
                        settingsNode.isCustomBookmark = true;
                        settingsNode.bookmarkID = Integer.parseInt(obj1.getString("status_id"));
                        settingsNode.parent.isCustomBookmark = true;
                        settingsNode.parent.bookmarkID = Integer.parseInt(obj1.getString("status_id"));
                        ItemNode settingsTable = new ItemNode(i, "Вид таблицы", settingsNode, R.drawable.circle, nodes, "table_settings", -1);
                        ItemNode settingsSingle = new ItemNode(i, "Вид записи", settingsNode, R.drawable.circle, nodes, "single_settings", -1);

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        } else if (ApplicationParameters.tl_current_statuses != null) {
            //транслайновские
            try {
                for (int j = 0; j < ApplicationParameters.tl_current_statuses.length(); j++) {
                    JSONObject obj = ApplicationParameters.tl_current_statuses.getJSONObject(j);
                    ItemNode node = new ItemNode(j, obj.getString("STATUS_NAME"), parent, R.drawable.circle, nodes, "cont_status", -1);
                    node.count = obj.getString("CNT");
                    ItemNode settingsNode = new ItemNode(0, "Настройки", node, R.drawable.circle, nodes, "bookmark_settings", -1);
                    settingsNode.isCustomBookmark = true;
                    settingsNode.bookmarkID = Integer.parseInt(obj.getString("STATUS_ID"));
                    settingsNode.parent.isCustomBookmark = true;
                    settingsNode.parent.bookmarkID = Integer.parseInt(obj.getString("STATUS_ID"));
                    ItemNode settingsTable = new ItemNode(0, "Вид таблицы", settingsNode, R.drawable.circle, nodes, "table_settings", -1);
                    ItemNode settingsSingle = new ItemNode(0, "Вид записи", settingsNode, R.drawable.circle, nodes, "single_settings", -1);

                }
            } catch (JSONException e) {

            }
            return;
        }
        for (int i = 0; i < ApplicationParameters.statuses.size(); i++) {
            //Канаваровские
            ItemNode node = new ItemNode(i, ApplicationParameters.statuses.get(i).getText(),
                    parent, R.drawable.circle, nodes,
                    "cont_status", -1);
            boolean fl = false;
            for (int j = 0; j < ApplicationParameters.status_counts.size(); j++) {
                if (ApplicationParameters.statuses.get(i).getName().equals(ApplicationParameters.status_counts.get(j).getName())) {
                    node.count = ApplicationParameters.status_counts.get(j).getText();
                    fl = true;
                    break;
                }
            }
            if (!fl) {
                node.count = "0";
            }
        }
    }


    public void setDeclarationStatuses(ItemNode parent) {
        if (ApplicationParameters.current_gtd_statuses != null && !ApplicationParameters.isCanavara) {
            //ТББ-шные
            try {
                JSONObject obj2 = null;
                for (int i = 0; i < ApplicationParameters.current_gtd_statuses.length(); i++) {
                    JSONObject obj = ApplicationParameters.current_gtd_statuses.getJSONObject(i);
                    if (obj.getString("STATUS_ID").equals("17")) {
                        obj2 = ApplicationParameters.current_gtd_statuses.getJSONObject(i);
                    } else {
                        ItemNode nd = new ItemNode(17, obj.getString("STATUS_NAME"), parent, R.drawable.circle, nodes, "gtd_status", -1);
                        nd.count = obj.getString("CNT");

                        ItemNode settingsNode = new ItemNode(17, "Настройки", nd, R.drawable.nd_settings, nodes, "bookmark_settings", -1);
                        settingsNode.isCustomBookmark = true;
                        settingsNode.bookmarkID = Integer.parseInt(obj.getString("STATUS_ID"));
                        settingsNode.parent.isCustomBookmark = true;
                        settingsNode.parent.bookmarkID = Integer.parseInt(obj.getString("STATUS_ID"));

                        ItemNode settingsTable = new ItemNode(17, "Вид таблицы", settingsNode, R.drawable.circle, nodes, "table_settings", -1);
                        ItemNode settingsSingle = new ItemNode(17, "Вид записи", settingsNode, R.drawable.circle, nodes, "single_settings", -1);
                    }

                }
                if (obj2 != null) {
                    ItemNode nd = new ItemNode(17, obj2.getString("STATUS_NAME"), parent, R.drawable.circle, nodes, "gtd_status", -1);
                    nd.count = obj2.getString("CNT");

                    ItemNode settingsNode = new ItemNode(17, "Настройки", nd, R.drawable.circle, nodes, "bookmark_settings", -1);
                    settingsNode.isCustomBookmark = true;
                    settingsNode.bookmarkID = Integer.parseInt(obj2.getString("STATUS_ID"));
                    settingsNode.parent.isCustomBookmark = true;
                    settingsNode.parent.bookmarkID = Integer.parseInt(obj2.getString("STATUS_ID"));

                    ItemNode settingsTable = new ItemNode(17, "Вид таблицы", settingsNode, R.drawable.circle, nodes, "table_settings", -1);
                    ItemNode settingsSingle = new ItemNode(17, "Вид записи", settingsNode, R.drawable.circle, nodes, "single_settings", -1);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        } else {
            ItemNode declarationAll = new ItemNode(7, "Все", parent, R.drawable.nd_declarations, nodes, "all_dec", -1);
            ItemNode declarationCur = new ItemNode(8, "Текущий месяц", parent, R.drawable.nd_declarations, nodes, "cur_month_dec", -1);
            ItemNode declarationLast = new ItemNode(9, "Предыдущий месяц", parent, R.drawable.nd_declarations, nodes, "last_month_dec", -1);
            ItemNode declarationArch = new ItemNode(10, "Архив", parent, R.drawable.nd_declarations, nodes, "archive_dec", -1);
        }
    }

    public void setDeclarationStatuses2(ItemNode parent) {
        if (ApplicationParameters.isCanavara) {
            //ТББ-шные
            try {
                if (ApplicationParameters.canavara_gtd_statuses == null)
                    ApplicationParameters.canavara_gtd_statuses = new JSONArray();
                JSONObject obj2 = null;
                for (int i = 0; i < ApplicationParameters.canavara_gtd_statuses.length(); i++) {
                    JSONObject obj = ApplicationParameters.canavara_gtd_statuses.getJSONObject(i);
                    if (obj.getString("STATUS_ID").equals("17")) {
                        ItemNode nd = new ItemNode(17, obj.getString("STATUS_NAME"), parent, R.drawable.circle, nodes, "gtd_status", -1);
                        nd.count = obj.getString("CNT");
                        nd.bookmarkID = Integer.parseInt(obj.getString("STATUS_ID"));
                    } else {
                        ItemNode nd = new ItemNode(17, obj.getString("STATUS_NAME"), parent, R.drawable.circle, nodes, "gtd_status", -1);
                        nd.count = obj.getString("CNT");
                        nd.bookmarkID = Integer.parseInt(obj.getString("STATUS_ID"));

                        /*ItemNode settingsNode = new ItemNode(17, "Настройки", nd, R.drawable.settings_white, nodes, "bookmark_settings");
                        settingsNode.isCustomBookmark = true;
                        settingsNode.bookmarkID = Integer.parseInt(obj.getString("STATUS_ID"));
                        settingsNode.parent.isCustomBookmark = true;
                        settingsNode.parent.bookmarkID = Integer.parseInt(obj.getString("STATUS_ID"));

                        ItemNode settingsTable = new ItemNode(17, "Вид таблицы", settingsNode, R.drawable.search_results_white, nodes, "table_settings");
                        ItemNode settingsSingle = new ItemNode(17, "Вид записи", settingsNode, R.drawable.single_record_white, nodes, "single_settings");*/
                    }

                }
                if (obj2 != null) {
                    ItemNode nd = new ItemNode(17, obj2.getString("STATUS_NAME"), parent, R.drawable.circle, nodes, "gtd_status", -1);
                    nd.count = obj2.getString("CNT");

                   /* ItemNode settingsNode = new ItemNode(17, "Настройки", nd, R.drawable.settings_white, nodes, "bookmark_settings");
                    settingsNode.isCustomBookmark = true;
                    settingsNode.bookmarkID = Integer.parseInt(obj2.getString("STATUS_ID"));
                    settingsNode.parent.isCustomBookmark = true;
                    settingsNode.parent.bookmarkID = Integer.parseInt(obj2.getString("STATUS_ID"));

                    ItemNode settingsTable = new ItemNode(17, "Вид таблицы", settingsNode, R.drawable.search_results_white, nodes, "table_settings");
                    ItemNode settingsSingle = new ItemNode(17, "Вид записи", settingsNode, R.drawable.single_record_white, nodes, "single_settings");*/
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        } else {
            ItemNode declarationAll = new ItemNode(7, "Все", parent, R.drawable.circle, nodes, "all_dec", -1);
            ItemNode declarationCur = new ItemNode(8, "Текущий месяц", parent, R.drawable.circle, nodes, "cur_month_dec", -1);
            ItemNode declarationLast = new ItemNode(9, "Предыдущий месяц", parent, R.drawable.circle, nodes, "last_month_dec", -1);
            ItemNode declarationArch = new ItemNode(10, "Архив", parent, R.drawable.circle, nodes, "archive_dec", -1);
        }
    }

    public void loadBookmarks() {
        SharedPreferences historyPrefs = getSharedPreferences("UserPreference", 0);
        Gson gson = new Gson();
        String jsonH = historyPrefs.getString("bookmarks", "");
        Type type = new TypeToken<ArrayList<Bookmark>>() {
        }.getType();
        if (jsonH != "") {
            ApplicationParameters.bookmarks = gson.fromJson(jsonH, type);
        }
    }

    private void setTestMenu() {
        loadBookmarks();
        nodes.clear();
        ItemNode refreshNode = new ItemNode(false, true, 0, "Обновить", null, R.drawable.nd_refresh, nodes, "refresh_request", -1);

        // ItemNode declarationsNode = new ItemNode(0, "Декларации", null, R.drawable.declarations, nodes, "declarations");
        ItemNode containerNode = new ItemNode(1, "Грузы", null, R.drawable.nd_containers, nodes, "containers", R.drawable.containers);
        setContainerStatuses(containerNode);/*
        ItemNode containerWait = new ItemNode(2, "Ожидаемые", containerNode, R.drawable.clock, nodes, "wait");
        ItemNode containerPort = new ItemNode(3, "В порту", containerNode, R.drawable.port, nodes, "port");
        ItemNode containerWay = new ItemNode(4, "В пути", containerNode, R.drawable.road, nodes, "way");
        ItemNode containerFinished = new ItemNode(5, "Завершенные", containerNode, R.drawable.finish, nodes, "finished");*/

        String decName = "Декларации";
        if (ApplicationParameters.isCanavara) decName = "Отчеты проводящего";
        ItemNode declarationNode = new ItemNode(6, decName, null, R.drawable.nd_declarations, nodes, "declarations", R.drawable.declarations_back);
        ItemNode declarationAll = new ItemNode(7, "Все", declarationNode, R.drawable.circle, nodes, "all_dec", -1);
        ItemNode declarationCur = new ItemNode(8, "Текущий месяц", declarationNode, R.drawable.circle, nodes, "cur_month_dec", -1);
        ItemNode declarationLast = new ItemNode(9, "Предыдущий месяц", declarationNode, R.drawable.circle, nodes, "last_month_dec", -1);
        ItemNode declarationArch = new ItemNode(10, "Архив", declarationNode, R.drawable.circle, nodes, "archive_dec", -1);
        ItemNode declarationNum = new ItemNode(11, "Найти по номеру", declarationNode, R.drawable.circle, nodes, "num_dec", -1);
        ItemNode declarationSearch = new ItemNode(11, "Расширенный поиск", declarationNode, R.drawable.circle, nodes, "search_dec", -1);
        if (ApplicationParameters.group_id != 2) {
            ItemNode dictNode = new ItemNode(6, "Справочники", null, R.drawable.nd_dicts, nodes, "dictionaries", R.drawable.dictionaries);
            ItemNode clientNode = new ItemNode(12, "Клиенты", dictNode, R.drawable.nd_clients, nodes, "clients", -1);
            ItemNode partnersNode = new ItemNode(13, "Партнеры", dictNode, R.drawable.nd_partners, nodes, "partners", -1);

            ItemNode parkNode = new ItemNode(12, "Перевозчики", dictNode, R.drawable.nd_drivers, nodes, "parks", -1);
            ItemNode terminalNode = new ItemNode(13, "Терминалы", dictNode, R.drawable.nd_terminals, nodes, "terminals", -1);

            ItemNode lineNode = new ItemNode(12, "Линии", dictNode, R.drawable.nd_lines, nodes, "lines", -1);
            ItemNode payersNode = new ItemNode(13, "Наши фирмы", dictNode, R.drawable.nd_firms, nodes, "payer", -1);
        }
        ItemNode bookmarksNode = new ItemNode(14, "Закладки", null, R.drawable.nd_bookmarks, nodes, "bookmarks", R.drawable.favorites);
        // ItemNode historyNode = new ItemNode(15, "История поиска", null, R.drawable.history, nodes, "history");
        ItemNode addBookmarksNode = new ItemNode(14, "Создать новую", bookmarksNode, R.drawable.circle, nodes, "add_bookmark", -1);

        setBookmarks(15, bookmarksNode);
        ItemNode settingsNode = new ItemNode(false, true, 15 + ApplicationParameters.bookmarks.size(), "Настройки", null, R.drawable.nd_settings, nodes, "settings", R.drawable.options);
        ItemNode settingsContNode = new ItemNode(false, true, 16 + ApplicationParameters.bookmarks.size(), "Грузы", settingsNode, R.drawable.nd_containers_settings, nodes, "cont_settings", -1);
        ItemNode settingsDecNode = new ItemNode(false, true, 16 + ApplicationParameters.bookmarks.size(), "Декларации", settingsNode, R.drawable.nd_declarations_settings, nodes, "dec_settings", -1);
        ItemNode settingsTable = new ItemNode(false, true, 16 + ApplicationParameters.bookmarks.size(), "Вид таблицы", settingsContNode, R.drawable.nd_table_view, nodes, "table_settings", -1);
        ItemNode settingsSingle = new ItemNode(false, true, 17 + ApplicationParameters.bookmarks.size(), "Вид записи", settingsContNode, R.drawable.nd_single_view, nodes, "single_settings", -1);
        ItemNode settingsDecTable = new ItemNode(false, true, 16 + ApplicationParameters.bookmarks.size(), "Вид таблицы", settingsDecNode, R.drawable.nd_table_view, nodes, "dec_table_settings", -1);
        ItemNode settingsDecSingle = new ItemNode(false, true, 17 + ApplicationParameters.bookmarks.size(), "Вид записи", settingsDecNode, R.drawable.nd_single_view, nodes, "dec_single_settings", -1);
        if (ApplicationParameters.isCanavara) {
            ItemNode settingsRepDecNode = new ItemNode(16 + ApplicationParameters.bookmarks.size(), "Отчеты проводящего", settingsNode, R.drawable.nd_declarations_settings, nodes, "dec_settings", -1);
            ItemNode settingsRepDecTable = new ItemNode(16 + ApplicationParameters.bookmarks.size(), "Вид таблицы", settingsRepDecNode, R.drawable.nd_table_view, nodes, "rep_dec_table_settings", -1);
            ItemNode settingsRepDecSingle = new ItemNode(17 + ApplicationParameters.bookmarks.size(), "Вид записи", settingsRepDecNode, R.drawable.nd_single_view, nodes, "rep_dec_single_settings", -1);
        }
        createReports(nodes);
        ItemNode aboutNode = new ItemNode(false, true, 0, "Обратная связь", null, R.drawable.nd_review, nodes, "review_set", -1);
        ItemNode reviewNode = new ItemNode(false, true, 0, "Оставить отзыв", aboutNode, R.drawable.nd_review, nodes, "review", -1);
        ItemNode contactsNode = new ItemNode(false, true, 0, "О разработчике", aboutNode, R.drawable.nd_review, nodes, "contacts", -1);
        ItemNode authNode = new ItemNode(false, true, 0, "Выход из системы", null, R.drawable.nd_exit, nodes, "authorize", -1);

    }

    public void createReports(ArrayList<ItemNode> nodes) {
        ItemNode reportsNode = new ItemNode(18, "Генератор отчетов", null, R.drawable.nd_reports, nodes, "testnode", R.drawable.reports);
        ItemNode addReportNode = new ItemNode(18, "Создать новый", reportsNode, R.drawable.nd_reports, nodes, "add_report_node", -1);
        ItemNode showReportNode = new ItemNode(18, "Готовые отчеты", reportsNode, R.drawable.nd_reports, nodes, "show_report_node", -1);
        for (int i = 0; i < ApplicationParameters.reports.size(); i++) {
            Report report = ApplicationParameters.reports.get(i);
            ItemNode reportNode = new ItemNode(18, report.getName(), showReportNode, R.drawable.nd_reports, nodes, "single_report_node", "", Integer.parseInt(Long.toString(report.getId())), -1);
        }
    }

    //меню закладок
    public void setBookmarks(int from, ItemNode parent) {
        for (int i = 0; i < ApplicationParameters.bookmarks.size(); i++) {
            ItemNode node = new ItemNode(from + i, ApplicationParameters.bookmarks.get(i).getName(),
                    parent, R.drawable.circle, nodes,
                    "bookmark_entity",
                    ApplicationParameters.bookmarks.get(i).getDescription(), (int) ApplicationParameters.bookmarks.get(i).getId(), -1);
            node.count = ApplicationParameters.bookmarks.get(i).count;
            ItemNode settingsNode = new ItemNode(16 + ApplicationParameters.bookmarks.size(), "Настройки", node, R.drawable.circle, nodes, "bookmark_settings", -1);
            settingsNode.isBookmark = true;
            settingsNode.bookmarkID = (int) ApplicationParameters.bookmarks.get(i).getId();
            ItemNode editNode = new ItemNode(16 + ApplicationParameters.bookmarks.size(), "Редактировать", node, R.drawable.circle, nodes, "edit_bookmark", -1);
            ItemNode removeNode = new ItemNode(16 + ApplicationParameters.bookmarks.size(), "Удалить", node, R.drawable.circle, nodes, "remove_bookmark", -1);

            ItemNode settingsTable = new ItemNode(16 + ApplicationParameters.bookmarks.size(), "Вид таблицы", settingsNode, R.drawable.circle, nodes, "table_settings", -1);
            ItemNode settingsSingle = new ItemNode(17 + ApplicationParameters.bookmarks.size(), "Вид записи", settingsNode, R.drawable.circle, nodes, "single_settings", -1);
        }
    }

    //формируем строку параметров расширенного поиска
    public String getJSONParameters(String chosenStatus) {
        String mainF = "";
        JSONArray arr = new JSONArray();
        JSONObject obj = new JSONObject();
        String f_name = "ID_STATUS_EX";
        String f_type = "dict";
        if (!(ApplicationParameters.current_statuses != null && ApplicationParameters.current_statuses.length() > 0) && !(ApplicationParameters.tl_current_statuses != null && ApplicationParameters.tl_current_statuses.length() > 0)) {
            f_name += "2";
            f_type = "string";
        }

        try {
            obj.put("field_name", f_name);
            obj.put("field_type", f_type);
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
        return getSharedPreferences("UserPreference", 0).getString(key, "123");
    }

    public String status;

    //расширенный поиск - сам запрос
    public JSONObject checkServerCredentials(final String filter, final int bookmark_id) throws JSONException {
        final String access_token = getPref("access_token");
        final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
        JSONParser parser = new JSONParser();
        JSONObject obj = parser.getJSONFromUrl("http://" + ApplicationParameters.MAIN_DOMAIN + "/search", new HashMap<String, String>() {{
            put("access_token", access_token);
            put("hs", secure_token);
            put("filter_object", getJSONParameters(filter));
            if (bookmark_id > 0) {
                put("is_custom_bookmark", "1");
                put("bookmark_id", Integer.toString(bookmark_id));
            }
        }}, getApplicationContext());
        return obj;
    }


    //поиск по декларациям - сам запрос
    public JSONObject checkServerCredentials2(final String type, final String num, final String offset) throws JSONException {
        final String access_token = getPref("access_token");
        final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
        JSONParser parser = new JSONParser();
        JSONObject obj = parser.getJSONFromUrl("http://" + ApplicationParameters.MAIN_DOMAIN + "/declarations", new HashMap<String, String>() {{
            put("access_token", access_token);
            put("hs", secure_token);
            put("type", ApplicationParameters.chosenDeclarationType);
            put("offset", offset);
            if (type.equals("num")) {
                put("num", num);
            }
        }}, getApplicationContext());
        return obj;
    }


    public void saveCredentials(String hash, String key) {
        SharedPreferences mPrefs = getSharedPreferences("UserPreference", 0);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(key, hash);
        editor.commit();
    }


    //расширенный поиск или поиск по пользовательской/кастомной закладке - задача в выделенном потоке
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
                jsonObject = checkServerCredentials(args[0], Integer.parseInt(args[1]));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            showProgress(false);
            if (res == null) {
                Toast.makeText(BaseLogisticActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
            } else if (res.has("error")) {
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
                            Intent intent = new Intent(BaseLogisticActivity.this, LoginActivity.class);
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(BaseLogisticActivity.this, SearchResults.class);
                try {
                    ApplicationParameters.lastResults = new JSONArray();
                    ApplicationParameters.tempResults = new JSONArray();
                    JSONArray tmp = new JSONArray(res.getString("result"));
                    int isCustomBookmark = 1;
                    int bookmarkID = -1;
                    try {
                        isCustomBookmark = res.getString("is_custom_bookmark") == null ? 1 : 0;
                        bookmarkID = res.getString("bookmark_id") != null ? Integer.parseInt(res.getString("bookmark_id")) : -1;
                    } catch (Exception e) {

                    }

                    String order = "";
                    if (bookmarkID > 0) {
                        intent.putExtra("order", res.getString("fields_order"));
                        intent.putExtra("bookmarkType", 1);
                        intent.putExtra("bookmark_id", bookmarkID);
                    }
                    ApplicationParameters.currentLength = Integer.parseInt(res.getString("count"));
                    ApplicationParameters.lastResults = tmp;
                    for (int i = 0; i < tmp.length(); i++) {
                        ApplicationParameters.tempResults.put(ApplicationParameters.lastResults.get(i));
                    }
                    ApplicationParameters.setTrafficDocuments(new JSONArray(res.getString("documents")));
                    // intent.putExtra("json_results", arr.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(intent);

                //return true;
            }
        }
    }

    public class Unregister extends AsyncTask<String, Void, JSONObject> {
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
                final String access_token = getPref("access_token");
                final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
                JSONParser parser = new JSONParser();
                jsonObject = parser.getJSONFromUrl("http://" + ApplicationParameters.MAIN_DOMAIN + "/user/unregister", new HashMap<String, String>() {{
                    put("access_token", access_token);
                    put("hs", secure_token);
                }}, getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            showProgress(false);
            if (res == null) {
                Toast.makeText(BaseLogisticActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
            } else {
                SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(ApplicationParameters.PROPERTY_REG_ID, "");
                editor.commit();


                ApplicationParameters.GCM_KEY = "";
                saveCredentials(ApplicationParameters.GCM_KEY, "gcm_key");
                logout();
            }

        }
    }


    private class BookmarkToken extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(final String... args) {
            if (ApplicationParameters.GCM_KEY.isEmpty()) return new JSONObject() {{
                try {
                    put("error", "err");
                    put("status_code", 401);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }};
            JSONObject jsonObject = null;
            final String access_token = getPref("access_token");
            final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
            JSONParser parser = new JSONParser();
            jsonObject = parser.getJSONFromUrl("http://" + ApplicationParameters.MAIN_DOMAIN + "/search/bookmark_search", new HashMap<String, String>() {{
                put("access_token", access_token);
                put("hs", secure_token);
                put("bookmark_id", args[0]);
            }}, getApplicationContext());
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            showProgress(false);
            if (res == null) {
                Toast.makeText(BaseLogisticActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
            } else if (res.has("error")) {
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
                            Intent intent = new Intent(BaseLogisticActivity.this, LoginActivity.class);
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(BaseLogisticActivity.this, SearchResults.class);
                try {
                    ApplicationParameters.lastResults = new JSONArray();
                    ApplicationParameters.tempResults = new JSONArray();
                    JSONArray tmp = new JSONArray(res.getString("result"));
                    ApplicationParameters.currentLength = Integer.parseInt(res.getString("count"));
                    ApplicationParameters.lastResults = tmp;
                    for (int i = 0; i < tmp.length(); i++) {
                        ApplicationParameters.tempResults.put(ApplicationParameters.lastResults.get(i));
                    }
                    ApplicationParameters.setTrafficDocuments(new JSONArray(res.getString("documents")));
                    intent.putExtra("order", res.getString("order"));
                    intent.putExtra("bookmark_id", Integer.parseInt(res.getString("bookmark_id")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(intent);

                //return true;
            }
        }
    }

    String tmp = "all";


    private class CheckToken3 extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... args) {
            if (ApplicationParameters.GCM_KEY.isEmpty()) return new JSONObject() {{
                try {
                    put("error", "err");
                    put("status_code", 401);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }};
            JSONObject jsonObject = null;
            String cc = "";
            ApplicationParameters.chosenDeclarationType = args[0];
            if (args.length > 1) {
                cc = args[1];

                ApplicationParameters.decType = cc;
            }
            String off = "0";
            if (args.length > 2 && !args[2].isEmpty()) off = args[2];
            try {

                jsonObject = checkServerCredentials2(args[0], ApplicationParameters.decType, off);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            showProgress(false);
            if (res == null) {
                Toast.makeText(BaseLogisticActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
            } else if (res.has("error")) {
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
                            Intent intent = new Intent(BaseLogisticActivity.this, LoginActivity.class);
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(BaseLogisticActivity.this, BaseSearchResults.class);
                try {

                    ApplicationParameters.lastResults = new JSONArray();
                    ApplicationParameters.tempResults = new JSONArray();
                    ApplicationParameters.currentLength = Integer.parseInt(res.getString("count"));
                    JSONArray tmp = new JSONArray(res.getString("result"));
                    ApplicationParameters.lastResults = tmp;
                    for (int i = 0; i < tmp.length(); i++) {
                        ApplicationParameters.tempResults.put(ApplicationParameters.lastResults.get(i));
                    }
                    try {
                        ApplicationParameters.setTrafficDocuments(new JSONArray(res.getString("documents")));
                        ApplicationParameters.setEDDocuments(new JSONArray(res.getString("eds")));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // intent.putExtra("json_results", arr.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(intent);

                //return true;
            }
        }
    }

    private class CheckToken4 extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... args) {
            if (ApplicationParameters.GCM_KEY.isEmpty()) return new JSONObject() {{
                try {
                    put("error", "err");
                    put("status_code", 401);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }};
            JSONObject jsonObject = null;
            String cc = "";
            ApplicationParameters.chosenDeclarationType = args[0];
            if (args.length > 1) {
                cc = args[1];

                ApplicationParameters.decType = cc;
            }
            String off = "0";
            if (args.length > 2 && !args[2].isEmpty()) off = args[2];
            try {

                jsonObject = checkServerCredentials2(args[0], ApplicationParameters.decType, off);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            showProgress(false);
            if (res == null) {
                Toast.makeText(BaseLogisticActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
            } else if (res.has("error")) {
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
                            Intent intent = new Intent(BaseLogisticActivity.this, LoginActivity.class);
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(BaseLogisticActivity.this, BaseReportsSearchResults.class);
                try {

                    ApplicationParameters.lastResults = new JSONArray();
                    ApplicationParameters.tempResults = new JSONArray();
                    ApplicationParameters.currentLength = Integer.parseInt(res.getString("count"));
                    JSONArray tmp = new JSONArray(res.getString("result"));
                    ApplicationParameters.lastResults = tmp;
                    for (int i = 0; i < tmp.length(); i++) {
                        ApplicationParameters.tempResults.put(ApplicationParameters.lastResults.get(i));
                    }
                    try {
                        ApplicationParameters.setTrafficDocuments(new JSONArray(res.getString("documents")));
                        ApplicationParameters.setEDDocuments(new JSONArray(res.getString("eds")));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // intent.putExtra("json_results", arr.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(intent);

                //return true;
            }
        }
    }

    private class RemoveBookmark extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(final String... args) {
            if (ApplicationParameters.GCM_KEY.isEmpty()) return new JSONObject() {{
                try {
                    put("error", "err");
                    put("status_code", 401);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }};
            JSONObject jsonObject = null;
            final String access_token = getPref("access_token");
            final String secure_token = LoginActivity.encryptStringSHA512(access_token + LoginActivity.encryptStringSHA512(getPref("login") + getPref("password")).toUpperCase() + ApplicationParameters.CLIENT_SECRET);
            JSONParser parser = new JSONParser();
            jsonObject = parser.getJSONFromUrl("http://" + ApplicationParameters.MAIN_DOMAIN + "/remove_bookmark", new HashMap<String, String>() {{
                put("access_token", access_token);
                put("hs", secure_token);
                put("bookmark_id", args[0]);
            }}, getApplicationContext());
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            showProgress(false);
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
                            Intent intent = new Intent(BaseLogisticActivity.this, LoginActivity.class);
                            intent.putExtra("login", true);
                            startActivity(intent);
                            finish();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                showProgress(false);
                try {
                    ApplicationParameters.removeBookmarkByID(res.getInt("bookmark_id"));
                    saveBookmarks();
                    saveCredentials(res.getString("access_token"), "access_token");
                    setDrawerMenu();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                //return true;
            }
        }
    }

    protected Dialog onCreateDialog(int id) {
        if (id == 2) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            // заголовок
            adb.setTitle("Предупреждение");
            // сообщение
            adb.setMessage("Найдено более 250 результатов, пожалуйста уточните критерии поиска");
            // иконка
            adb.setIcon(android.R.drawable.ic_dialog_info);

            //adb.setNeutralButton("Ок", myClickListener);
            return adb.create();
        }
        return super.onCreateDialog(id);
    }

    public View mProgressView;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                    if (show)
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    else
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            if (show)
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            else
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    public void nodeAction(ItemNode node) {
        if (ApplicationParameters.GCM_KEY.isEmpty()) {
            if (!ApplicationParameters.loginOpened) {
                ApplicationParameters.loginOpened = true;
                Intent intent = new Intent(BaseLogisticActivity.this, LoginActivity.class);
                intent.putExtra("login", true);
                startActivity(intent);
                finish();
            }
        }
        if (node.key.equals("authorize")) {
            authorize();
        } else if (node.key.equals("refresh_request")) {
            if (BaseLogisticActivity.this instanceof MainActivity) {
                ApplicationParameters.cached = false;
            }
            refreshRequest();
        } else if (node.key.equals("review")) {
            review();
        } else if (node.key.equals("contacts")) {
            if (BaseLogisticActivity.this instanceof ContactsActivity) {
                return;
            } else {
                Intent intent = new Intent(BaseLogisticActivity.this, ContactsActivity.class);
                startActivity(intent);
            }
        } else if (node.key.equals("testnode")) {
            if (BaseLogisticActivity.this instanceof GroupReportActivity) {
                return;
            } else {
                Intent intent = new Intent(BaseLogisticActivity.this, GroupReportActivity.class);
                startActivity(intent);
            }
        } else if (node.key.equals("clients")) {
            Intent intent = new Intent(BaseLogisticActivity.this, ClientsActivity.class);
            intent.putExtra("adapterType", "LIST_CLIENT");
            startActivity(intent);
        } else if (node.key.equals("partners")) {
            Intent intent = new Intent(BaseLogisticActivity.this, ClientsActivity.class);
            intent.putExtra("adapterType", "LIST_CUSTOMER");
            startActivity(intent);
        } else if (node.key.equals("terminals")) {
            Intent intent = new Intent(BaseLogisticActivity.this, ClientsActivity.class);
            intent.putExtra("adapterType", "LIST_TERMINAL");
            startActivity(intent);
        } else if (node.key.equals("payer")) {
            Intent intent = new Intent(BaseLogisticActivity.this, ClientsActivity.class);
            intent.putExtra("adapterType", "LIST_PAYER");
            startActivity(intent);
        } else if (node.key.equals("parks")) {
            Intent intent = new Intent(BaseLogisticActivity.this, ClientsActivity.class);
            intent.putExtra("adapterType", "LIST_PARK");
            startActivity(intent);
        } else if (node.key.equals("lines")) {
            Intent intent = new Intent(BaseLogisticActivity.this, ClientsActivity.class);
            intent.putExtra("adapterType", "LIST_LINE");
            startActivity(intent);
        } else if (node.key.equals("cont_status")) {
            showProgress(true);
            if (ApplicationParameters.customBookmarks.get(node.bookmarkID) != null)
                (new CheckToken()).execute(node.description, Integer.toString(ApplicationParameters.customBookmarks.get(node.bookmarkID).first));
            else
                (new CheckToken()).execute(node.description, Integer.toString(-1));
        } else if (node.key.equals("gtd_status")) {
            showProgress(true);
            (new CheckToken3()).execute("status" + node.bookmarkID);
        } else if (node.key.equals("transit_arch")) {
            showProgress(true);
            (new CheckToken3()).execute(node.key);
        } else if (node.key.equals("transit_cur")) {
            showProgress(true);
            (new CheckToken3()).execute(node.key);
        } else if (node.key.equals("archive_dec")) {
            showProgress(true);
            if (ApplicationParameters.isCanavara) {
                (new CheckToken4()).execute("arc");
            } else {
                (new CheckToken3()).execute("arc");
            }
        } else if (node.key.equals("all_dec")) {
            showProgress(true);
            if (ApplicationParameters.isCanavara) {
                (new CheckToken4()).execute("all");
            } else {
                (new CheckToken3()).execute("all");
            }
        } else if (node.key.equals("last_month_dec")) {
            showProgress(true);
            if (ApplicationParameters.isCanavara) {
                (new CheckToken4()).execute("last");
            } else {
                (new CheckToken3()).execute("last");
            }
        } else if (node.key.equals("table_settings")) {
            if (node.parent != null && node.parent.isBookmark)
                startSettings(1, node.parent.bookmarkID);
            else if (node.parent != null && node.parent.isCustomBookmark)
                startSettings(1, node.parent.bookmarkID, true);
            else
                startSettings(1);
        } else if (node.key.equals("dec_table_settings")) {
            startSettings(6);
        } else if (node.key.equals("dec_single_settings")) {
            startSettings(7);
        } else if (node.key.equals("rep_dec_table_settings")) {
            startSettings(8);
        } else if (node.key.equals("rep_dec_single_settings")) {
            startSettings(9);
        } else if (node.key.equals("push_settings")) {
            startSettings(0);
        } else if (node.key.equals("single_settings")) {
            if (node.parent != null && node.parent.isBookmark)
                startSettings(2, node.parent.bookmarkID);
            else if (node.parent != null && node.parent.isCustomBookmark)
                startSettings(2, node.parent.bookmarkID, true);
            else
                startSettings(2);
        } else if (node.key.equals("cur_month_dec")) {
            showProgress(true);
            if (ApplicationParameters.isCanavara) {
                (new CheckToken4()).execute("cur");
            } else {
                (new CheckToken3()).execute("cur");
            }
        } else if (node.key.equals("add_bookmark")) {
            Intent intent = new Intent(BaseLogisticActivity.this, NewSearchActivity.class);
            startActivity(intent);
        } else if (node.key.equals("edit_bookmark")) {
            try {
                String desc = ApplicationParameters.getBookmarkDescByID(node.parent.bookmarkID);
                ApplicationParameters.lastSearchString = new JSONObject(desc);
                Intent intent = new Intent(BaseLogisticActivity.this, NewSearchActivity.class);
                intent.putExtra("bookmarkID", node.parent.bookmarkID);
                startActivity(intent);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (node.key.equals("remove_bookmark")) {
            showProgress(true);
            (new RemoveBookmark()).execute(Integer.toString(node.parent.bookmarkID));
        } else if (node.key.equals("search_dec")) {
            Intent intent = new Intent(BaseLogisticActivity.this, BaseNewSearchActivity.class);
            startActivity(intent);
        } else if (node.key.equals("num_dec")) {
            showProgress(true);
            final EditText input = new EditText(BaseLogisticActivity.this);
            final TripleTableField[] book = new TripleTableField[1];

            new AlertDialog.Builder(BaseLogisticActivity.this)
                    .setTitle("Поиск по номеру")
                    .setMessage("Введите номер декларации или его часть для поиска")
                    .setView(input)
                    .setPositiveButton("Найти", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            try {
                                String value = input.getText().toString();
                                if (ApplicationParameters.isCanavara) {
                                    (new CheckToken4()).execute("num", value);
                                } else {
                                    (new CheckToken3()).execute("num", value);
                                }
                            } catch (Exception e) {

                            }

                        }
                    }).setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Do nothing.
                }
            }).show();
        } else if (node.isBookmark) {
            try {
                String desc = ApplicationParameters.getBookmarkDescByID(node.bookmarkID);
                ApplicationParameters.lastSearchString = new JSONObject(desc);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            showProgress(true);
            (new BookmarkToken()).execute(Integer.toString(node.bookmarkID));

        }

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(
                AdapterView<?> parent, View view, int position, long id
        ) {
            ItemNode node = nodes.get(position);
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
                nodeAction(node);
                // myDrawerLayer.closeNavigationDrawer();
                myDrawerLayout.closeDrawer(myDrawerLayer);
            }
            //           displayView(position);
        }
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pushes.add(new PushItem(intent.getStringExtra("date"), intent.getStringExtra("title"), intent.getStringExtra("description")));
            TextView tv = (TextView) BaseLogisticActivity.this.findViewById(R.id.counter);
            SharedPreferences unread = getSharedPreferences("UserPreference", 0);
            int unread_count = unread.getInt("unread", 0);
            tv.setText(Integer.toString(unread_count));
            tv.setVisibility(unread_count == 0 ? View.GONE : View.VISIBLE);
        }
    };


    protected void setAdditionalParams() {

    }

    public void getVersion() {

    }

    public void getDeveloper() {

    }

    public void review() {
        final EditText input = new EditText(BaseLogisticActivity.this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(BaseLogisticActivity.this)
                .setTitle("Отзыв")
                .setMessage("Введите описание ошибки")
                .setView(input)
                .setPositiveButton("Отправить", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                String str = "";
                                for (int i = 0; i < ApplicationParameters.request_history.size(); i++) {
                                    str += Integer.toString(i + 1) + ".  " + ApplicationParameters.request_history.get(i).toString() + "\n";
                                }
                                JSONParser parser = new JSONParser();
                                final String str2 = str;
                                parser.getJSONFromUrl("http://service.g-soft.ru/error/new", new HashMap<String, String>() {{
                                    put("error", input.getText().toString() + "\n\n" + str2);
                                    put("login", getPref("login"));
                                }}, getApplicationContext());
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void msg) {
                                ;
                            }
                        }.execute(null, null, null);

                    }
                });
        final AlertDialog alertClient = builder.create();
        alertClient.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.base, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (myDrawerLayout.isDrawerOpen(Gravity.RIGHT))
                myDrawerLayout.closeDrawer(Gravity.RIGHT);
            else
                myDrawerLayout.openDrawer(Gravity.RIGHT);
            return true;
        }
        if (id == R.id.action_home) {
            if (myDrawerLayout.isDrawerOpen(Gravity.RIGHT))
                myDrawerLayout.closeDrawer(Gravity.RIGHT);
            else
                myDrawerLayout.openDrawer(Gravity.RIGHT);
            return true;
        }
        if (id == R.id.action_refresh) {
            refreshRequest();
        }
        if (id == R.id.action_settings) {
            if (BaseLogisticActivity.this instanceof SearchResults) {
                int bid = ((SearchResults) BaseLogisticActivity.this).bookmarkID;
                if (bid > 0)
                    if (((SearchResults) BaseLogisticActivity.this).bookmarkType == 1) {
                        startSettings(1, bid, true);
                    } else {
                        startSettings(1, bid);
                    }
                else
                    startSettings(1);
            }
            if (BaseLogisticActivity.this instanceof ViewPagerSearchActivity) {
                int bid = ((ViewPagerSearchActivity) BaseLogisticActivity.this).bookmarkID;
                if (bid > 0)
                    if (((ViewPagerSearchActivity) BaseLogisticActivity.this).bookmarkType == 1) {
                        startSettings(2, bid, true);
                    } else {
                        startSettings(2, bid);
                    }

                else
                    startSettings(2);
            }
            if (BaseLogisticActivity.this instanceof BaseSearchResults) {
                startSettings(6);
            }
            if (BaseLogisticActivity.this instanceof BaseViewPagerSearchActivity) {
                startSettings(7);
            }

            if (BaseLogisticActivity.this instanceof BaseReportsSearchResults) {
                startSettings(8);
            }
            if (BaseLogisticActivity.this instanceof BaseReportsViewPagerSearchActivity) {
                startSettings(9);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    String SENDER_ID = "665149531559";

    public void logout() {
        final SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
        SharedPreferences.Editor editor = prefs.edit();
        Intent unregIntent = new Intent("com.google.android.c2dm.intent.UNREGISTER");
        unregIntent.putExtra(SENDER_ID, PendingIntent.getBroadcast(this, 0, new Intent(), 0));

        unregIntent.setPackage("com.google.android");
        unregIntent.setClassName("com.google.android.c2dm.intent", "UNREGISTER");
        startService(unregIntent);
        editor.putString(ApplicationParameters.PROPERTY_REG_ID, "");
        editor.commit();

        SharedPreferences historyPrefs = getSharedPreferences("UserPreference", 0);
        SharedPreferences.Editor prefsEditor = historyPrefs.edit();
        prefsEditor.putString("login", "SYSDBA");
        prefsEditor.putString("password", "posdfkkskjmfds32");
        prefsEditor.putString("access_token", "123");
        prefsEditor.commit();
        Intent intent = new Intent(BaseLogisticActivity.this, LoginActivity.class);
        intent.putExtra("login", true);
        startActivity(intent);
        //finish();
    }


    public boolean authorize() {
        (new Unregister()).execute();
        return true;
    }
}
