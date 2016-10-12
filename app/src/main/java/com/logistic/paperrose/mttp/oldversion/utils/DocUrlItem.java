package com.logistic.paperrose.mttp.oldversion.utils;

/**
 * Created by paperrose on 15.03.2016.
 */
public class DocUrlItem {
    public String url;
    public int type = 0;

    public DocUrlItem(String url) {
        this.url = url;
    }

    public DocUrlItem(String url, int type) {
        this.url = url;
        this.type = type;
    }
}
