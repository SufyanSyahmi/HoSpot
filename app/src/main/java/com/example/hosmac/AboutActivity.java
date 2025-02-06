package com.example.hosmac;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Find the back button
        ImageView backButton = findViewById(R.id.back_button);

        // Set click listener for the back button
        backButton.setOnClickListener(v -> onBackPressed()); // Navigate back
    }
}
