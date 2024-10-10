package com.example.omnitrace.database.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class App {
    public App(@NonNull String bundleIdentifier) {
        this.bundleIdentifier = bundleIdentifier;
    }

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "bundle_identifier")
    public String bundleIdentifier;
}