package com.example.signmateapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.OpenCVLoader;


public class Login extends AppCompatActivity {
    Button b1 ;
    Button b2 ;
    TextView tv1;
    ImageView p1_img;
    EditText user;
    EditText pass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        if (OpenCVLoader.initDebug()) Log.d("OpenCV App", "Success.....");

        user = findViewById(R.id.user1);
        pass = findViewById(R.id.pass1);
        tv1 = findViewById(R.id.p1_tv1);
        b1 = findViewById(R.id.p1b1);
        p1_img = findViewById(R.id.p1_img1);

        b1.setOnClickListener(
                v -> {
                    if(user.getText().toString().equals("sagarmishra") && pass.getText().toString().equals("8657")){
                        Toast.makeText(Login.this,"Login Successful!",Toast.LENGTH_SHORT).show();
                        Intent i = new Intent( Login.this,Dashboard.class);
                        startActivity(i);
                    }else{
                        Toast.makeText(Login.this,"Login Failed!\t try this- username:'sagarmishra' & Password: '8657'",Toast.LENGTH_LONG).show();
                    }


                }
        );

        b2 = findViewById(R.id.p1b2);
        b2.setOnClickListener(
                v -> {
                    Intent i1 = new Intent( Login.this,signUp.class);
                    startActivity(i1);
                }
        );
    }
}
