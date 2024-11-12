package com.example.omnitrace.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.DeleteTable;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.omnitrace.database.models.App;
import com.example.omnitrace.database.models.Permission;

import java.util.List;

@Dao
public interface AppDao {
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

    @Query("SELECT * FROM App")
    List<App> getAllApps();

    @Query("SELECT DISTINCT name FROM Permission")
    List<String> getAllUniquePermissions();

    @Query("SELECT * FROM Permission WHERE app_id = :appId")
    List<Permission> getPermissionsForApp(long appId);

    @Query("SELECT appName FROM App")
    List<String> getAllPackageNames();

    @Query("SELECT * FROM App WHERE isMalicious = 1 AND isOverridden = 0")
    List<App> getNonOverriddenMaliciousApps();

    @Query("SELECT * FROM App WHERE appName = :packageName LIMIT 1")
    App getAppByPackageName(String packageName);
}