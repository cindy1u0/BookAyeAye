package com.example.bookaye;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// MainActivity of BookAye
public class BookAye extends AppCompatActivity {

    String inputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_aye);

        final Integer CAMERA_REQ_CODE = 1;
        // Checks for camera permissions. If not set then requests permission.
        if (!checkPermission(Manifest.permission.CAMERA, CAMERA_REQ_CODE))
            requestPermission(Manifest.permission.CAMERA, CAMERA_REQ_CODE);

        // Sets editor action listener when Enter is pressed in input Edit Text
        final EditText textInput = findViewById(R.id.textView3);
        textInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Closes keyboard
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    // Stores our input text
                    inputText = textInput.getText().toString();

                    // Redirects to activity 2
                    openActivity2();

                    return true;
                }
                return false;
            }
        });

    }

    // Redirects to activity 2
    public void openActivity2(){
        Intent i = new Intent(this, Activity2.class);
        i.putExtra("bookName", inputText);
        startActivity(i);
    }

    // Function to check permission
    public boolean checkPermission(String permission, int requestCode)
    {
        // Checking if permission is not granted
        return !(ContextCompat.checkSelfPermission(BookAye.this, permission) == PackageManager.PERMISSION_DENIED);
    }

    // Function to request permission
    public void requestPermission(String permission, int requestCode)
    {
        if (checkPermission(permission, requestCode)) {
            Toast.makeText(BookAye.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(BookAye.this, new String[] { permission }, requestCode);
        }
    }
}
