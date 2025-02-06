package com.example.hosmac;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeActivity extends AppCompatActivity {

    private TextView greetingText;
    private Button checkNearestHospitalButton;

    // Firebase Authentication and Database references
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        greetingText = findViewById(R.id.greetingText);
        checkNearestHospitalButton = findViewById(R.id.checkNearestHospitalButton);

        // Check if user is logged in
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in. Redirecting to login...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, activity_login.class));
            finish();
            return;
        }

        // Fetch the user's name
        fetchUserName();


        // Button click listener for the nearest hospital check
        checkNearestHospitalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
    }

    private void fetchUserName() {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String userName = task.getResult().child("name").getValue(String.class);
                greetingText.setText("Hello, nice to meet you," +" " + (userName != null ? userName : "User") + "!");
            } else {
                greetingText.setText("Hello,nice to meet you, User!");
            }
        }).addOnFailureListener(e -> {
            greetingText.setText("Hello, nice to meet you, User!");
            Toast.makeText(HomeActivity.this, "Welcome user", Toast.LENGTH_SHORT).show();
        });
    }
}