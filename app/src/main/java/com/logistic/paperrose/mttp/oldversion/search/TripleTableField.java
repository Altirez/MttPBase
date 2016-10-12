package com.logistic.paperrose.mttp.oldversion.search;

import com.logistic.paperrose.mttp.oldversion.settings.TableField;

/**
 * Created by paperrose on 23.01.2015.
 */
public class TripleTableField extends TableField {

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    String type = "string";
    public String additional;
    public int id;
    TripleTableField() {
        super();
    }

    public TripleTableField(String text) {
        super(text);
    }

    public TripleTableField(String name, String text) {
        super(name, text);
    }

    public TripleTableField(String name, String text, String type) {
        super(name, text);
        this.type = type;
    }

    public TripleTableField(String name, String text, String type, int id) {
        super(name, text);
        this.type = type;
        this.id = id;
    }

    public TripleTableField(String name, String text, String type, String id_name) {
        super(name, text);
        this.type = type;
        additional = id_name;
    }

    public TripleTableField(String name, String text, String type, String id_name, int id) {
        super(name, text);
        this.type = type;
        additional = id_name;
        this.id = id;
    }

}
