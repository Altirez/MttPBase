package com.logistic.paperrose.mttp.newversion.db.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by paperrose on 30.06.2016.
 */
@Table(name = "TableField")
public class TableField extends Model{
    //    @SerializedName("ID")
    @Column(name = "DbId")
    public int dbId; //ИД в таблице. Нужно для определения порядка
    //    @SerializedName("TITLE")
    @Column(name = "DbTitle")
    public String dbTitle; //Наименование колонки
    //    @SerializedName("NAME")
    @Column(name = "DbName")
    public String dbName; //Имя поля
    //   @SerializedName("TYPE")
    @Column(name = "DbType")
    public String dbType; //Тип данных в поле. string, date, bool, dict, float
    //   @SerializedName("TABLE")
    @Column(name = "DbTable")
    public String dbTable; //Таблица, которой принадлежит поле

    //поля для типа dict
    //   @SerializedName("FIELD_TABLE")
    @Column(name = "DbDictTable")
    public String dbDictTable; //Название связанного справочника
    //   @SerializedName("REAL_TABLE_NAME")
    @Column(name = "DbDictFieldName")
    public String dbDictFieldName; //Какое поле выводим из справочника при связке
    //   @SerializedName("DICT_ID_NAME")
    @Column(name = "DbDictLinkName")
    public String dbDictLinkName; //Имя поля, связывающего со справочником

}
