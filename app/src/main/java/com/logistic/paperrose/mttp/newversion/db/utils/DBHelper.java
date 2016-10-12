package com.logistic.paperrose.mttp.newversion.db.utils;

import android.content.Context;

import com.activeandroid.ActiveAndroid;
import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.newversion.db.models.DbTables;
import com.logistic.paperrose.mttp.newversion.db.models.MenuNode;

/**
 * Created by paperrose on 04.07.2016.
 */
public class DBHelper {
    private Context mContext;

    public DBHelper(Context context) {
        this.mContext = context;
        ActiveAndroid.initialize(mContext);
        DbTables item = DbTables.load(DbTables.class, 1);
        if (item == null) {
            //первичное создание/очистка базы.
            createTables();
        }
    }

    private void createTables() {
        final DbTables traffic;
        final DbTables declarations;
        final DbTables dictionaries;
        final DbTables bookmarks;
        final DbTables settings;
        DbTables reports;
        (traffic = new DbTables(){{
            name = "traffic";
            visibility = 1;
            title = "Грузы";
            image = R.drawable.containers;
            icon = R.drawable.nd_containers;
        }}).save();

        (declarations = new DbTables(){{
            name = "declarations";
            visibility = 1;
            title = "Декларации";
            image = R.drawable.declarations_back;
            icon = R.drawable.nd_declarations;
        }}).save();

        (dictionaries = new DbTables(){{
            name = "dictionaries";
            visibility = 1;
            title = "Справочники";
            image = R.drawable.dictionaries;
            icon = R.drawable.nd_dicts;
        }}).save();

        (bookmarks = new DbTables(){{
            name = "bookmarks";
            visibility = 1;
            title = "Закладки";
            image = R.drawable.favorites;
            icon = R.drawable.nd_bookmarks;
        }}).save();

        (settings = new DbTables(){{
            name = "settings";
            visibility = 1;
            title = "Настройки";
            image = R.drawable.options;
            icon = R.drawable.nd_settings;
        }}).save();

        (reports = new DbTables(){{
            name = "reports";
            visibility = 1;
            title = "Генератор отчетов";
            image = R.drawable.reports;
            icon = R.drawable.nd_reports;
        }}).save();

        MenuNode trafficNum;
        MenuNode trafficExt;

        MenuNode gtdNum;
        MenuNode gtdExt;

        MenuNode dictClients;
        MenuNode dictPartners;
        MenuNode dictDrivers;
        MenuNode dictTerminals;
        MenuNode dictLines;
        MenuNode dictPayers;

        MenuNode bookmarksAdd;
/*      MenuNode bookmarksEdit;
        MenuNode bookmarksSettings;
        MenuNode bookmarksSettingsTable;
        MenuNode bookmarksSettingsSingle;
        MenuNode bookmarksDelete;
*/

        final MenuNode settingsTraffic;
        MenuNode settingsTrafficTable;
        MenuNode settingsTrafficSingle;
        final MenuNode settingsGtd;
        MenuNode settingsGtdTable;
        MenuNode settingsGtdSingle;


        (trafficNum = new MenuNode() {{
            name = "trafficNum";
            title = "Найти по номеру";
            icon = -1;
            dbTable = traffic;
            parentNode = null;
            count = -1;
        }}).save();
        (trafficExt = new MenuNode() {{
            name = "trafficExt";
            title = "Расширенный поиск";
            icon = -1;
            dbTable = traffic;
            parentNode = null;
            count = -1;
        }}).save();

        (gtdNum = new MenuNode() {{
            name = "gtdNum";
            title = "Найти по номеру";
            icon = -1;
            dbTable = declarations;
            parentNode = null;
            count = -1;
        }}).save();
        (gtdExt = new MenuNode() {{
            name = "gtdExt";
            title = "Расширенный поиск";
            icon = -1;
            dbTable = declarations;
            parentNode = null;
            count = -1;
        }}).save();

        (dictClients = new MenuNode() {{
            name = "dictClients";
            title = "Клиенты";
            icon = R.drawable.nd_clients;
            dbTable = dictionaries;
            parentNode = null;
            count = -1;
        }}).save();
        (dictPartners = new MenuNode() {{
            name = "dictPartners";
            title = "Партнеры";
            icon = R.drawable.nd_partners;
            dbTable = dictionaries;
            parentNode = null;
            count = -1;
        }}).save();
        (dictDrivers = new MenuNode() {{
            name = "dictDrivers";
            title = "Перевозчики";
            icon = R.drawable.nd_drivers;
            dbTable = dictionaries;
            parentNode = null;
            count = -1;
        }}).save();
        (dictTerminals = new MenuNode() {{
            name = "dictTerminals";
            title = "Терминалы";
            icon = R.drawable.nd_terminals;
            dbTable = dictionaries;
            parentNode = null;
            count = -1;
        }}).save();
        (dictLines = new MenuNode() {{
            name = "dictLines";
            title = "Линии";
            icon = R.drawable.nd_lines;
            dbTable = dictionaries;
            parentNode = null;
            count = -1;
        }}).save();
        (dictPayers = new MenuNode() {{
            name = "dictPayers";
            title = "Наши фирмы";
            icon = R.drawable.nd_firms;
            dbTable = dictionaries;
            parentNode = null;
            count = -1;
        }}).save();

        (bookmarksAdd = new MenuNode() {{
            name = "bookmarksAdd";
            title = "Создать новую";
            icon = -1;
            dbTable = bookmarks;
            parentNode = null;
            count = -1;
        }}).save();

        (settingsTraffic = new MenuNode() {{
            name = "settingsTraffic";
            title = "Грузы";
            icon = R.drawable.nd_containers_settings;
            dbTable = settings;
            parentNode = null;
            count = -1;
        }}).save();
        (settingsTrafficTable = new MenuNode() {{
            name = "settingsTrafficTable";
            title = "Вид таблицы";
            icon = R.drawable.nd_table_view;
            dbTable = settings;
            parentNode = settingsTraffic;
            count = -1;
        }}).save();
        (settingsTrafficSingle = new MenuNode() {{
            name = "settingsTrafficSingle";
            title = "Вид записи";
            icon = R.drawable.nd_single_view;
            dbTable = settings;
            parentNode = settingsTraffic;
            count = -1;
        }}).save();
        (settingsGtd = new MenuNode() {{
            name = "settingsGtd";
            title = "Декларации";
            icon = R.drawable.nd_declarations_settings;
            dbTable = settings;
            parentNode = null;
            count = -1;
        }}).save();
        (settingsGtdTable = new MenuNode() {{
            name = "settingsGtdTable";
            title = "Вид таблицы";
            icon = R.drawable.nd_table_view;
            dbTable = settings;
            parentNode = settingsGtd;
            count = -1;
        }}).save();
        (settingsGtdSingle = new MenuNode() {{
            name = "settingsGtdSingle";
            title = "Вид записи";
            icon = R.drawable.nd_single_view;
            dbTable = settings;
            parentNode = settingsGtd;
            count = -1;
        }}).save();
    }


}
