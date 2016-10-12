package com.logistic.paperrose.mttp.newversion.db.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.ArrayList;

/**
 * Created by paperrose on 04.07.2016.
 */
@Table(name= "MenuNode")
public class MenuNode extends Model {
    @Column(name = "DbTable")
    public DbTables dbTable;
    @Column(name = "NodeName")
    public String name;
    @Column(name = "NodeTitle")
    public String title;
    @Column(name = "NodeCount")
    public int count;
    @Column(name = "ParentNode")
    public MenuNode parentNode;
    @Column(name = "Icon")
    public int icon;


    public ArrayList<MenuNode> items() {
        ArrayList<MenuNode> nodes = new ArrayList<>();
        nodes.addAll(getMany(MenuNode.class, "ParentNode"));
        return nodes;
    }
}
