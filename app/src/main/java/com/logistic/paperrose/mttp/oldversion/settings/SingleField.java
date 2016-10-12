package com.logistic.paperrose.mttp.oldversion.settings;

/**
 * Created by paperrose on 27.01.2015.
 */
public class SingleField {
    String text = "";

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public SingleField() {

    }

    public SingleField(String text) {
        this.text = text;
    }
}
