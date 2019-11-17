package com.example.bookaye;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class BookAye extends AppCompatActivity {

    String inputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_aye);

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
        startActivity(i);
    }
}
