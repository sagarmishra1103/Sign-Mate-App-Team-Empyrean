package com.example.signmateapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class vToGesture extends AppCompatActivity {
    Button b1,b2;
    EditText ed1;
    ImageView imgview;
    ImageButton SearchButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v_to_gesture);

        b1 = findViewById(R.id.vtg_b);
        b1.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent( vToGesture.this,endPage.class);
                        startActivity(i);
                    }
                }
        );
        ed1=findViewById(R.id.edit_text);
        b2 = findViewById(R.id.vtg_b2);
        b2.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent( vToGesture.this,Dashboard.class);
                        startActivity(i);
                    }
                }
        );

        imgview = findViewById(R.id.img_view);
        SearchButton = findViewById(R.id.Search_button);
        SearchButton.setOnClickListener(v -> {
            if (ed1 != null && ed1.getText().toString().equals("ASL")) {
                imgview.setImageResource(R.drawable.asl);
            } else {
                Toast.makeText(v.getContext(), "Invalid!\nTry to Search ASL", Toast.LENGTH_SHORT).show();
            }
        });



    }
}