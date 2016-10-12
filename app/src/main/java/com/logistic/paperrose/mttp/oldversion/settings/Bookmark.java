package com.logistic.paperrose.mttp.oldversion.settings;

/**
 * Created by paperrose on 24.02.2015.
 */
public class Bookmark {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String name;
    private String description;
    public int type = 1;
    private long id;
    public String order;

    public String count = "";

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public Bookmark(long id, String name, String description, String order) {
        this.name = name;
        this.id = id;
        this.description = description;
        this.order = order;
    }

    public Bookmark(long id, String name, String description, String order, int type) {
        this.name = name;
        this.id = id;
        this.description = description;
        this.order = order;
        this.type = type;
    }

}
