package com.example.omnitrace;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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

        // Convert set to array and sort it for consistent order in the CSV
        String[] permissionsArray = permissionSet.toArray(new String[0]);
        java.util.Arrays.sort(permissionsArray);

        // Define the internal storage file path
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