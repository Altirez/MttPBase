package com.logistic.paperrose.mttp.newversion.db.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by paperrose on 04.07.2016.
 */
@Table(name = "UserOptions")
public class UserOptions extends Model {
    @Column(name = "AccessToken")
    public String accessToken;
    @Column(name = "GroupId")
    public int group_id;

}
