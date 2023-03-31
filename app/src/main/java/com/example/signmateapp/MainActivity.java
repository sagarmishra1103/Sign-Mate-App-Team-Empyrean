package com.example.signmateapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {
    Button b1 ;
    ImageView iv1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (OpenCVLoader.initDebug()) Log.d("OpenCV App", "Success.....");


        b1 =  findViewById(R.id.main_b1);
        iv1 = findViewById(R.id.iv1);

        b1.setOnClickListener(
                v -> {
                    Intent i = new Intent(MainActivity.this,Login.class);
                    startActivity(i);
                }

        );

    }

}