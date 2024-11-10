package com.example.omnitrace;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

        Intent intent = getIntent();
        List<String> packageNames = (List<String>) intent.getSerializableExtra("PACKAGE_NAMES");
        List<Integer> clusterLabels = (List<Integer>) intent.getSerializableExtra("CLUSTER_LABELS");


        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(WarningActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        imageList = new ArrayList<>();
        packageNames.remove(0);
        if (clusterLabels != null){
            for (int i = 0; i < clusterLabels.size(); i++){
                if (clusterLabels.get(i) == 1){
                    imageList.add(new ImageItem(R.drawable.red_warning_icon, packageNames.get(i)));
                }
            }
        } else {
            imageList.add(new ImageItem(R.drawable.no_warning_icon, "No warnings!"));
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager((layoutManager));

        adapter = new ImageAdapter(imageList);
        recyclerView.setAdapter(adapter);

        layoutManager = new LinearLayoutManager(this);
    }
}
