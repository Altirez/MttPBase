package com.logistic.paperrose.mttp.oldversion.pushes;

/**
 * Created by paperrose on 30.12.2014.
 */
public class PushItem {


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    String date;
    String title;
    String description;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String type = "2";
    public String record_id;

    PushItem(String date, String title, String description) {
        this.date = date;
        this.title = title;
        this.description = description;
    }

    PushItem(String date, String title, String description, String type, String record_id) {
        this.date = date;
        this.title = title;
        this.description = description;
        this.type = type;
        this.record_id = record_id;
    }
}
