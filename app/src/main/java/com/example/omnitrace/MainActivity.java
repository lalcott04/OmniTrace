package com.example.omnitrace;

import android.os.Bundle;
//import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.util.Log;

import com.chaquo.python.PyObject;
import com.chaquo.python.android.PyApplication;
import com.example.omnitrace.database.AppDao;
import com.example.omnitrace.database.AppDatabase;
import com.example.omnitrace.database.models.App;
import com.example.omnitrace.database.models.Permission;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.chaquo.python.PyException;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "CSVReader";
    private Handler handler;
    private Runnable pollingRunnable;


    public List<Integer> getClusterLabels(String filePath, int numClusters) {

        if (! Python.isStarted()) {
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

    private List<String> readCsvAndGetPackageNames() {
        String filePath = "/data/data/com.example.omnitrace/files/app_permissions.csv";
        File csvFile = new File(filePath);
        List<String> packageNames = new ArrayList<>(); // Initialize a list to hold PackageNames

        if (csvFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
                String line;
                int lineCount = 0;

                while ((line = br.readLine()) != null) {
                    lineCount++;
                    // Split the line by commas
                    String[] columns = line.split(",");

                    // Ensure there is at least one column (PackageName)
                    if (columns.length > 0) {
                        String packageName = columns[0];  // Get the first column (PackageName)
                        packageNames.add(packageName);  // Add the PackageName to the list
                        Log.d(TAG, "Line " + lineCount + " PackageName: " + packageName); // Optional logging
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error reading CSV file: " + e.getMessage(), e);
            }
        } else {
            Log.e(TAG, "CSV file does not exist at: " + filePath);
        }

        return packageNames; // Return the list of PackageNames
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        handler = new Handler();
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                // Perform the polling operation here
                getPackages();
                pollData();

                // Schedule the next execution after 1 minute (60000 milliseconds)
                handler.postDelayed(this, 15000);
            }
        };
        handler.post(pollingRunnable);
    }

    private void pollData() {

        // TO DO IN POLLING SETUP:

        // Get applications

        // Write to CSV / Database

        // Model Analysis

        // Label / Package Name Mapping

        // UI Integration

        List<String> packagenames = readCsvAndGetPackageNames();

        List<Integer> returned = getClusterLabels("/data/data/com.example.omnitrace/files/app_permissions.csv", 2);
        for (Integer label : returned) {
            Log.d("ClusterLabel", "Label: " + label);
        }
    }

    public void getPackages() {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> appsList = pm.queryIntentActivities(intent, 0);

        // Collect permission data to pass to Python for clustering
        Set<String> permissionSet = new HashSet<>();
        for (ResolveInfo info : appsList) {
            String packageName = info.activityInfo.packageName;
            //List<Integer> permissionList = new ArrayList<>();
            try {
                PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
                if (packageInfo.requestedPermissions != null) {
                    for (String permission : packageInfo.requestedPermissions) {
                        permissionSet.add(permission);
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            exportPermissionsToCSV(permissionSet, appsList, pm);
        }

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


    /*
        // Get PackageManager to retrieve apps

        /*
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



    /*

    */
