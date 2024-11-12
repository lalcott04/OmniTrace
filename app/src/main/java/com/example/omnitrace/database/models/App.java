package com.example.omnitrace.database.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class App {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String appName;
    private boolean isMalicious;
    private boolean isOverridden;// New field to track if the alert has been dismissed

    public App(String appName, boolean isMalicious) {
        this.appName = appName;
        this.isMalicious = isMalicious;
        this.isOverridden = false; // default to false
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public boolean isMalicious() {
        return isMalicious;
    }

    public void setMalicious(boolean malicious) {
        this.isMalicious = malicious;
    }

    public boolean isOverridden() {
        return isOverridden;
    }

    public void setOverridden(boolean overridden) {
        this.isOverridden = overridden;
    }

    public String getPackageName() {
        return appName;
    }

    public int getAppId() {
        return id;
    }
}
