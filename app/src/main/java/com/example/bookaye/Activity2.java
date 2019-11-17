package com.example.bookaye;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Activity2 extends AppCompatActivity {

    // Two buttons
    Button scan;
    Button selectFromPics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        scan = findViewById(R.id.buttonScan);
        selectFromPics = findViewById(R.id.buttonPic);

        // If scan button is clicked, open the camera
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Opens up camera
                openActivity3();
            }
        });

        // If the second button is clicked, open photo album
        selectFromPics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity4();
            }
        });

    }

    // opens activity 3
    public void openActivity3(){
        Intent i = new Intent(this, Activity3.class);
        startActivity(i);
    }

    // opens activity 4
    public void openActivity4(){
        Intent i = new Intent(this, Activity4.class);
        startActivity(i);
    }

}
