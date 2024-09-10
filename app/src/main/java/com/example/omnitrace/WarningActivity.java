package com.example.omnitrace;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

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

        backBtn = (ImageButton) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(WarningActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        imageList = new ArrayList<>();
        imageList.add(new ImageItem(R.drawable.warning_icon, "Image 1 description"));
        imageList.add(new ImageItem(R.drawable.warning_icon, "image 2 description"));
        imageList.add(new ImageItem(R.drawable.warning_icon, "image 3 description"));
        imageList.add(new ImageItem(R.drawable.warning_icon, "image 4 description"));
        imageList.add(new ImageItem(R.drawable.warning_icon, "image 5 description"));
        imageList.add(new ImageItem(R.drawable.warning_icon, "image 6 description"));
        imageList.add(new ImageItem(R.drawable.warning_icon, "image 7 description"));
        imageList.add(new ImageItem(R.drawable.warning_icon, "image 8 description"));
        imageList.add(new ImageItem(R.drawable.warning_icon, "image 9 description"));
        imageList.add(new ImageItem(R.drawable.warning_icon, "image 10 description"));

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager((layoutManager));

        adapter = new ImageAdapter(imageList);
        recyclerView.setAdapter(adapter);

        layoutManager = new LinearLayoutManager(this);
    }
}
