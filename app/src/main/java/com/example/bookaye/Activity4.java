package com.example.bookaye;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.android.gms.vision.text.TextRecognizer;

public class Activity4 extends AppCompatActivity {

    ImageView image;
    Button pick;

    private static final int PICK_CODE = 1000;
    private static final int PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_4);

        // View
        image = findViewById(R.id.image_view);
        pick = findViewById(R.id.buttonPic);

        // handle clicking action
        /*pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checks runtime permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission((Manifest.permission.READ_EXTERNAL_STORAGE)) == PackageManager.PERMISSION_DENIED) {
                        // Request permission
                        String[] perm = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        // Pop-up window to show permission denied
                        requestPermissions(perm, PERMISSION);
                    } else {
                        // Permission granted
                        pickFromGallery();
                    }
                } else {
                    // pick the picture from the gallery
                    pickFromGallery();
                }
            }
        });*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission((Manifest.permission.READ_EXTERNAL_STORAGE)) == PackageManager.PERMISSION_DENIED) {
                // Request permission
                String[] perm = {Manifest.permission.READ_EXTERNAL_STORAGE};
                // Pop-up window to show permission denied
                requestPermissions(perm, PERMISSION);
            } else {
                // Permission granted
                pickFromGallery();
            }
        } else {
            // pick the picture from the gallery
            pickFromGallery();
        }
    }

    public void pickFromGallery() {
        // pick an image
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromGallery();
                } else {
                    // permission denied
                    Toast.makeText(this, "Sorry no permission!", Toast.LENGTH_SHORT).show();
                }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_CODE) {
            // set image to image view
            image.setImageURI(data.getData());
        }
    }
}