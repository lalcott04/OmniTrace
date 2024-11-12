package com.example.omnitrace;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.Python;
import com.chaquo.python.PyObject;
import com.chaquo.python.android.AndroidPlatform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Handler handler;
    private Runnable pollingRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Warning button setup
        ImageButton warningBtn = findViewById(R.id.warningBtn);
        updateWarningIcon(); // Call this function to set up the warning icon initially

        warningBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WarningActivity.class);
            intent.putExtra("PACKAGE_NAMES", (ArrayList<String>) getPackageNamesFromCSV());
            intent.putExtra("CLUSTER_LABELS", (ArrayList<Integer>) getClusterLabels("/data/data/com.example.omnitrace/files/app_permissions.csv", 2));
            startActivity(intent);
        });
    }

    // Method to update warning icon
    private void updateWarningIcon() {
        ImageButton warningBtn = findViewById(R.id.warningBtn);
        TextView warningText = findViewById(R.id.warningText);

        List<String> maliciousApps = getMaliciousAppsFromCSV();
        Log.e(TAG, "Malicious apps size: " + maliciousApps.size());

        if (!maliciousApps.isEmpty()) {
            warningBtn.setImageResource(R.drawable.red_warning_icon);
            warningText.setText("View Security Warnings");
        } else {
            warningBtn.setImageResource(R.drawable.no_warning_icon);
            warningText.setText("No Security Warnings");
        }
    }

    public List<Integer> getClusterLabels(String filePath, int numClusters) {
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        Python python = Python.getInstance();
        PyObject pythonFile = python.getModule("kmeansclustering");  // Matches your .py file name

        // Call the get_cluster_labels function in the Python script
        PyObject labels = pythonFile.callAttr("get_cluster_labels", filePath, numClusters);

        // Convert the PyObject result to a List<Integer> in Java
        List<Integer> labelList = new ArrayList<>();
        for (PyObject label : labels.asList()) {
            labelList.add(label.toInt());
        }

        return labelList;
    }

    private void exportAllAppPermissionsToCSV() {
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
        exportPermissionsToCSV(permissionSet, appsList, pm);
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

        try (FileWriter writer = new FileWriter(csvFile, true)) {  // Append mode enabled
            // First, check if the file already exists and read it to check for existing packages
            Set<String> existingPackages = new HashSet<>();
            if (csvFile.exists()) {
                // Read the existing CSV to get already written package names
                BufferedReader reader = new BufferedReader(new FileReader(csvFile));
                String line;
                reader.readLine();  // Skip the header line
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(",");
                    existingPackages.add(data[0]);  // Add package name to the set of existing packages
                }
                reader.close();
            }

            // If CSV does not exist or is empty, write the header
            if (!csvFile.exists() || csvFile.length() == 0) {
                writer.append("PackageName");
                for (String permission : permissionsArray) {
                    writer.append(',').append(permission);
                }
                writer.append('\n');
            }

            // Write app permissions data only for apps that are not already in the CSV
            for (ResolveInfo info : appsList) {
                String packageName = info.activityInfo.packageName;

                // Skip writing if the package already exists in the CSV file
                if (existingPackages.contains(packageName)) {
                    continue;  // Skip this app since it's already in the CSV
                }

                // If not already in the file, write the app's data
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

    private List<String> getPackageNamesFromCSV() {
        List<String> packageNames = new ArrayList<>();
        File csvFile = new File(getFilesDir(), "app_permissions.csv");

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            // Skip the header
            reader.readLine();

            // Read each line (app) and extract package names
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                packageNames.add(data[0]);  // Package name is the first column
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return packageNames;
    }

    private void updateAppMaliciousStatusInCSV(String packageName, boolean isMalicious) {
        File csvFile = new File(getFilesDir(), "app_permissions.csv");

        try {
            // Read all lines from the CSV file
            BufferedReader reader = new BufferedReader(new FileReader(csvFile));
            StringBuilder fileContent = new StringBuilder();
            String line;
            boolean appUpdated = false;

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data[0].equals(packageName)) {
                    // Update the malicious status (assuming the malicious status is the last column)
                    data[data.length - 1] = isMalicious ? "1" : "0";
                    line = String.join(",", data);
                    appUpdated = true;
                }
                fileContent.append(line).append("\n");
            }
            reader.close();

            // If the app was updated, rewrite the CSV file
            if (appUpdated) {
                FileWriter writer = new FileWriter(csvFile);
                writer.write(fileContent.toString());
                writer.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getMaliciousAppsFromCSV() {
        List<String> maliciousApps = new ArrayList<>();
        File csvFile = new File(getFilesDir(), "app_permissions.csv");

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            // Skip the header
            reader.readLine();

            // Read each line (app) and check if it is malicious
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                String packageName = data[0];  // Package name is the first column
                String isMalicious = data[data.length - 1];  // Malicious status is the last column

                // If the last column is '1', the app is considered malicious
                if ("1".equals(isMalicious)) {
                    maliciousApps.add(packageName);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return maliciousApps;
    }
}
