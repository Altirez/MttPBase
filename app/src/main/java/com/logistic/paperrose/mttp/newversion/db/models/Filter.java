package com.logistic.paperrose.mttp.newversion.db.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by paperrose on 28.06.2016.
 */
@Table(name = "Filter")
public class Filter extends Model {
    @Column(name = "FilterValue")
    public String filterValue;
    @Column(name = "FilterField")
    public TableField tableField;
    @Column(name = "Bookmark")
    public Bookmark bookmark;
}
