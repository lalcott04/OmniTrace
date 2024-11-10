package com.example.omnitrace.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.omnitrace.database.models.App;
import com.example.omnitrace.database.models.Permission;

@Database(entities = {App.class, Permission.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AppDao dao();
}
