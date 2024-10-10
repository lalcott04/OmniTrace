package com.example.omnitrace;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.example.omnitrace.database.AppDao;
import com.example.omnitrace.database.AppDatabase;
import com.example.omnitrace.database.models.App;
import com.example.omnitrace.database.models.Permission;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Step 1: Get the package manager
        PackageManager pm = getPackageManager();

        // Step 2: Create the intent to query apps with a launcher icon
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        // Step 3: Query all apps with launcher activity
        List<ResolveInfo> appsList = pm.queryIntentActivities(intent, 0);

        // Collect all unique permissions
        Set<String> permissionSet = new HashSet<>();
        // Iterate permissionSets
        for (ResolveInfo info : appsList) {
            //Get Package Name
            String packageName = info.activityInfo.packageName;
            try {
                // Get Packageinfo based off PackageName
                PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
                // Store permissions in string array
                String[] requestedPermissions = packageInfo.requestedPermissions;
                // If exists
                if (requestedPermissions != null) {
                    // For each permission, add to permission hashset
                    for (String permission : requestedPermissions) {
                        permissionSet.add(permission);
                    }
                }
                //catch error
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }


        // Export to CSV
        extractToCSV(permissionSet, appsList, pm);


        // to locate the database go to /data/data/com.example.omnitrace/database/permissions.db
        // right click download, then you can sqlite3 the SQL database
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "permissions.db").allowMainThreadQueries().build();

        AppDao dao = db.dao();

        dao.deleteAllPermissions();
        dao.deleteAllApps();
        dao.resetIds();

        for (ResolveInfo info : appsList) {
            String packageName = info.activityInfo.packageName;


            App app = new App(packageName);
            long appId = dao.insertApp(app);

            try {
                PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
                String[] requestedPermissions = packageInfo.requestedPermissions;

                if (requestedPermissions != null) {
                    for (String permissionName : requestedPermissions) {
                        Permission permission = new Permission(appId, permissionName, true);
                        dao.insertPermission(permission);
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }


        db.close();
    }


    // Function that controls exporting to CSV
    private void extractToCSV(Set<String> permissionSet, List<ResolveInfo> appsList, PackageManager pm) {
        // Convert set to array and sort it for consistent order in the CSV
        String[] permissionsArray = permissionSet.toArray(new String[0]);
        java.util.Arrays.sort(permissionsArray);

        // Define the internal storage file path
        // saves the file to Device Explorer data/data/com.example.omnitrace/files
        // to open the Device Explorer -> View -> Tool Windows -> Device Explorer
        File csvFile = new File(getFilesDir(), "app_permissions.csv");
        FileWriter writer = null;

        try {
            // Create CSV file and write headers
            writer = new FileWriter(csvFile);
            writer.append("PackageName");
            for (String permission : permissionsArray) {
                writer.append(',')
                        .append(permission);
            }
            writer.append('\n');

            // Write each app's permissions
            for (ResolveInfo info : appsList) {
                String packageName = info.activityInfo.packageName;
                writer.append(packageName);

                // Initialize permission existence map with all zeros
                boolean[] permissionExists = new boolean[permissionsArray.length];
                try {
                    PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
                    String[] requestedPermissions = packageInfo.requestedPermissions;
                    if (requestedPermissions != null) {
                        for (String permission : requestedPermissions) {
                            int index = java.util.Arrays.binarySearch(permissionsArray, permission);
                            if (index >= 0) {
                                permissionExists[index] = true;
                            }
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                // Write permissions data
                for (boolean exists : permissionExists) {
                    writer.append(',')
                            .append(exists ? "1" : "0");
                }
                writer.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}