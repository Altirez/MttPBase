package com.logistic.paperrose.mttp.oldversion.utils;

import java.util.Date;

/**
 * Created by paperrose on 28.04.2015.
 */
public class LogRecord {
    public String text;
    public String time;

    public LogRecord(String text) {
        this.text = (new Date()).toString() + " / ---  --- / " +  text;
    }

    public LogRecord(String text, boolean withoutTime) {
        this.text = text;
    }

    public String toString() {
        return text;
    }
}
