package com.example.omnitrace;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class WarningActivity extends Activity {

    ImageButton backBtn;
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    List<ImageItem> imageList;

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warnings);

        //Getting lists from MainActivity.java
        Intent intent = getIntent();
        List<String> packageNames = (List<String>) intent.getSerializableExtra("PACKAGE_NAMES");
        List<Integer> clusterLabels = (List<Integer>) intent.getSerializableExtra("CLUSTER_LABELS");

        //Back button functionality (return to main menu)
        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> {
            Intent intent1 = new Intent(WarningActivity.this, MainActivity.class);
            startActivity(intent1);
        });

        //Displays the warnings that appear from the cluster list
        imageList = loadListFromPreferences();
        if (packageNames != null && clusterLabels != null){
            for (int i = 0; i < clusterLabels.size(); i++){
                if (clusterLabels.get(i) == 1){
                    imageList.add(new ImageItem(R.drawable.red_warning_icon, packageNames.get(i)));
                }
            }
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager((layoutManager));
        adapter = new ImageAdapter(imageList, this);
        recyclerView.setAdapter(adapter);
    }

    public void saveListToPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences("com.example.omnitrace", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();

        for (int i = 0; i < imageList.size(); i++){
            editor.putInt("imageResource_" + i, imageList.get(i).getImageResource());
            editor.putString("description_" + i, imageList.get(i).getDescription());
        }
        editor.apply();
    }

    private List<ImageItem> loadListFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("com.example.omnitrace", MODE_PRIVATE);
        List<ImageItem> list = new ArrayList<>();
        int i = 0;
        // Loop to check if the data exists
        while (sharedPreferences.contains("imageResource_" + i)) {
            int imageResource = sharedPreferences.getInt("imageResource_" + i, -1);
            String description = sharedPreferences.getString("description_" + i, "");
            if (imageResource != -1) {
                list.add(new ImageItem(imageResource, description));
            }
            i++;
        }
        return list;
    }
}
