package com.logistic.paperrose.mttp.oldversion.settings;

/**
 * Created by paperrose on 03.06.2015.
 */
public class BaseBookmark {

    private long id;
    public String order;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public BaseBookmark(long id, String order) {
        this.id = id;
        this.order = order;
    }

}
