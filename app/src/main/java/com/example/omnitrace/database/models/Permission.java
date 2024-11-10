package com.example.omnitrace.database.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Permission {
    public Permission(long appId, @NonNull String name, boolean enabled) {
        this.appId = appId;
        this.name = name;
        this.enabled = enabled;
    }

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "app_id")
    public long appId;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "enabled")
    public boolean enabled;
}

