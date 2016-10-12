package com.logistic.paperrose.mttp.newversion.db.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.ArrayList;

/**
 * Created by paperrose on 01.07.2016.
 */
@Table(name = "DbTable")
public class DbTables extends Model{
    @Column(name = "TableName")
    public String name;
    @Column(name = "TableTitle")
    public String title;
    @Column(name = "Visibility")
    public int visibility;
    @Column(name = "Orders")
    public String orders;
    @Column(name = "Image")
    public int image;
    @Column(name = "Icon")
    public int icon;

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
