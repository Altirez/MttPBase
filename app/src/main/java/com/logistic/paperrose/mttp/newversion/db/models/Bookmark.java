package com.logistic.paperrose.mttp.newversion.db.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paperrose on 01.07.2016.
 */
@Table(name = "Bookmark")
public class Bookmark extends Model{
    @Column(name = "CommonFilter")
    public String commonFilter;
    @Column(name = "FilterValue")
    public String filterValue;
    @Column(name = "FilterField")
    public TableField tableField;
    @Column(name = "Orders")
    public String orders;

    public List<Filter> items() {
        return getMany(Filter.class, "Bookmark");
    }

    public ArrayList<TableField> tableFields() {
        ArrayList<TableField> fields = new ArrayList<>();
        String [] tableOrders = orders.split("<<<")[0].split(">");
        for (String order : tableOrders) {
            try {
                fields.add(
                        (TableField)new Select()
                                .from(TableField.class)
                                .where("DbId = ?", Integer.parseInt(order))
                                .execute().get(0)
                );
            } catch (NullPointerException e) {

            }

        }
        return fields;
    }

    public ArrayList<TableField> recordFields() {
        ArrayList<TableField> fields = new ArrayList<>();
        String [] tableOrders = orders.split("<<<")[1].split(">");
        for (String order : tableOrders) {
            try {
                fields.add(
                        (TableField)new Select()
                                .from(TableField.class)
                                .where("DbId = ?", Integer.parseInt(order))
                                .execute().get(0)
                );
            } catch (NullPointerException e) {

            }
        }
        return fields;
    }
}
