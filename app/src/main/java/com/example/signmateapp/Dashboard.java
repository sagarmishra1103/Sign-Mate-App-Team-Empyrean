package com.example.signmateapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Dashboard extends AppCompatActivity {
    Button b1 ;
    Button b2 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        b1 = findViewById(R.id.Dash_B1);
        b1.setOnClickListener(
                v -> {
                    Intent i = new Intent( Dashboard.this,gToVoice.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
        );
        b2 = findViewById(R.id.Dash_B2);
        b2.setOnClickListener(
                v -> {
                    Intent i = new Intent( Dashboard.this,vToGesture.class);
                    startActivity(i);
                }
        );

    }
}