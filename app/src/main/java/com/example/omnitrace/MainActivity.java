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

        // Get PackageManager to retrieve apps
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        // Query apps with launcher activity
        List<ResolveInfo> appsList = pm.queryIntentActivities(intent, 0);

        // Collect all unique permissions from apps
        Set<String> permissionSet = new HashSet<>();
        for (ResolveInfo info : appsList) {
            String packageName = info.activityInfo.packageName;
            try {
                PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
                String[] requestedPermissions = packageInfo.requestedPermissions;
                if (requestedPermissions != null) {
                    for (String permission : requestedPermissions) {
                        permissionSet.add(permission);
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        // Export app permissions to CSV
        exportPermissionsToCSV(permissionSet, appsList, pm);

        // Initialize Room database
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "permissions.db").allowMainThreadQueries().build();
        AppDao dao = db.dao();

        // Clear previous data and reset IDs
        dao.deleteAllPermissions();
        dao.deleteAllApps();
        dao.resetIds();

        // Insert app and permission data into Room DB
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

        // Close database connection
        db.close();
    }

    /**
     * Exports app permissions to a CSV file in internal storage.
     *
     * @param permissionSet Set of unique permissions across all apps.
     * @param appsList      List of apps to retrieve permissions for.
     * @param pm            PackageManager to query app information.
     */
    private void exportPermissionsToCSV(Set<String> permissionSet, List<ResolveInfo> appsList, PackageManager pm) {
        String[] permissionsArray = permissionSet.toArray(new String[0]);
        java.util.Arrays.sort(permissionsArray);

        // Define the file path for saving CSV
        File csvFile = new File(getFilesDir(), "app_permissions.csv");

        try (FileWriter writer = new FileWriter(csvFile)) {
            // Write CSV headers
            writer.append("PackageName");
            for (String permission : permissionsArray) {
                writer.append(',').append(permission);
            }
            writer.append('\n');

            // Write app permissions data
            for (ResolveInfo info : appsList) {
                String packageName = info.activityInfo.packageName;
                writer.append(packageName);

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

                // Append permissions (1 or 0) for each app
                for (boolean exists : permissionExists) {
                    writer.append(',').append(exists ? "1" : "0");
                }
                writer.append('\n');
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
