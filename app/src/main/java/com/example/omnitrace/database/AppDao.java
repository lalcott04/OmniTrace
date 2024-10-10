package com.example.omnitrace.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.DeleteTable;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.omnitrace.database.models.App;
import com.example.omnitrace.database.models.Permission;

@Dao
public interface AppDao {
//    @Query("SELECT * FROM user")
//    List<User> getAll();
//
//    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
//    List<User> loadAllByIds(int[] userIds);
//
//    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    User findByName(String first, String last);

    @Insert
    long insertPermission(Permission permission);
    @Insert
    long insertApp(App app);

    @Query("UPDATE sqlite_sequence SET seq=0 WHERE name IN ('App', 'Permission');")
    void resetIds();
    @Query("DELETE FROM App;")
    void deleteAllApps();
    @Query("DELETE FROM Permission;")
    void deleteAllPermissions();
}