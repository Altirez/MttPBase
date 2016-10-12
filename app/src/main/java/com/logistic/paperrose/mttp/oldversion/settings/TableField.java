package com.logistic.paperrose.mttp.oldversion.settings;

/**
 * Created by paperrose on 12.01.2015.
 */
public class TableField extends SingleField {
    public String name = "";

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public TableField() {

    }

    public TableField(String text) {
        super(text);
    }

    public TableField(String name, String text) {
        super(text);
        this.name = name;
    }
}
