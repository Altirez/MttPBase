package com.logistic.paperrose.mttp.oldversion.settings;

import java.util.ArrayList;

/**
 * Created by paperrose on 26.02.2015.
 */
public class TableEntity {
    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public ArrayList<TableEntity> getEntities() {
        return entities;
    }

    public void setEntities(ArrayList<TableEntity> entities) {
        this.entities = entities;
    }

    public void addEntity(TableEntity entity) {
        this.entities.add(entity);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    int tableId;
    String tableName;
    ArrayList<TableEntity> entities = new ArrayList<TableEntity>();

    public TableEntity(int tableId, String tableName) {
        this.tableId = tableId;
        this.tableName = tableName;
    }
}
