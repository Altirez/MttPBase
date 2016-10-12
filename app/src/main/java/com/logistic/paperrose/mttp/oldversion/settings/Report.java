package com.logistic.paperrose.mttp.oldversion.settings;

/**
 * Created by paperrose on 22.07.2015.
 */
public class Report {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupFields() {
        return groupFields;
    }

    public void setGroupFields(String groupFields) {
        this.groupFields = groupFields;
    }

    public String getFilterString() {
        return filterString;
    }

    public void setFilterString(String filterString) {
        this.filterString = filterString;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    private String name;
    private String groupFields;
    private String filterString;
    private long id;

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String order;

    public Report(long id, String name, String groupFields, String filterString, String order) {
        this.name = name;
        this.id = id;
        this.groupFields = groupFields;
        this.order = order;
        this.filterString = filterString;
    }
}
