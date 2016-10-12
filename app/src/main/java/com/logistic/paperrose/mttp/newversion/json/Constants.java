package com.logistic.paperrose.mttp.newversion.json;

/**
 * Created by paperrose on 29.06.2016.
 */
public class Constants {
    public final static String RECEIVER = "JsonResultReceiver";
    public final static int STATUS_STARTED = 0;
    public final static int STATUS_SUCCESS = 1;
    public final static int STATUS_ERROR = -1;

    public enum Methods {

        AUTH, //логин
        LOGOUT, //логаут
        LOAD_INFO, //стартовый запрос. загрузка всех счетчиков, полей пользователя и прочего
        GET_PUSHES, //загрузка пушей
        GET_ED, //загрузка ЭД сообщений
        SEARCH_TRAFFIC, //поиск контейнеров
        SEARCH_TRAFFIC_BY_NUM, //поиск контейнера по номеру
        SEARCH_GTD, //поиск ГТД
        SEARCH_GTD_BY_NUM, //поиск ГТД по номеру
        AUTOCOMPLETE, //запрос на автозаполнение полей фильтра
        SEARCH_DICT, //автозаполнение по справочникам
        EDIT_TRAFFIC, //редактирование поля контейнера
        EDIT_GTD, //редактрирование поля ГТД
        DICT, //?
        ADD_BOOKMARK, //добавление закладки
        EDIT_BOOKMARK, //редактирование закладки
        EDIT_BOOKMARK_ORDER, //редактирование полей в закладке
        CHECK_AUTH, //?
        SAVE_SETTINGS, //сохранение списка полей
        UPLOAD_DOCUMENT //загрузка документа на сервер
    }
}
