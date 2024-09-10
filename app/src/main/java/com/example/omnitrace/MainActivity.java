package com.example.omnitrace;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    ImageButton settingsBtn;
    ImageButton warningBtn;
    TextView warningText;
    Button iconTestBtn;
    int iconCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        settingsBtn = (ImageButton) findViewById(R.id.settingsBtn);
        settingsBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        warningBtn = (ImageButton) findViewById(R.id.warningBtn);
        warningBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, WarningActivity.class);
                if (Objects.equals(warningBtn.getDrawable().getConstantState(), ResourcesCompat.getDrawable(getResources(), R.drawable.warning_icon, null).getConstantState())){
                    startActivity(intent);
                }
            }
        });

        iconTestBtn = (Button) findViewById(R.id.iconTestBtn);
        warningText = (TextView) findViewById(R.id.warningText);
        iconTestBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                iconTestBtn.setSelected(!iconTestBtn.isPressed());
                if (iconTestBtn.isPressed() && iconCount == 0){
                    warningBtn.setImageResource(R.drawable.no_warning_icon);
                    warningText.setText("No Security Warnings");
                    iconCount = 1;
                }
                else
                {
                    warningBtn.setImageResource(R.drawable.warning_icon);
                    warningText.setText("View Security Warnings");
                    iconCount = 0;
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}