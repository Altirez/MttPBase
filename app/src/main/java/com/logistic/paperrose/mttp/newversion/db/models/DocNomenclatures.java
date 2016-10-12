package com.logistic.paperrose.mttp.newversion.db.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by paperrose on 04.07.2016.
 */
@Table(name = "DocNomenclatures")
public class DocNomenclatures extends Model{
    @Column(name = "DbId")
    public int dbId;
    @Column(name = "Title")
    public String title;
}
