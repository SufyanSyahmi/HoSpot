package com.example.hosmac;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail;
    private ImageView profileImage;
    private Button btnLogout;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI elements
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        profileImage = findViewById(R.id.profileImage);
        btnLogout = findViewById(R.id.btnLogout);

        // Get the current user
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // Fetch and display user details
            fetchUserName(user.getUid());
            tvUserEmail.setText(user.getEmail());

            // Set a default profile image from the drawable folder
            profileImage.setImageResource(R.drawable.ic_profile_placeholder);
        } else {
            Toast.makeText(this, "User is not authenticated", Toast.LENGTH_SHORT).show();
        }

        // ✅ Fix: Proper Logout Functionality
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut(); // Sign out from Firebase
            Toast.makeText(ProfileActivity.this, "Logged out", Toast.LENGTH_SHORT).show();

            // Redirect to Login Page
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Close ProfileActivity
        });
    }

    private void fetchUserName(String userId) {
        mDatabase.child("users").child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String userName = task.getResult().child("name").getValue(String.class);
                tvUserName.setText(userName != null ? userName : "No Name Available");
            } else {
                tvUserName.setText("No Name Available");
            }
        }).addOnFailureListener(e -> {
            tvUserName.setText("No Name Available");
            Toast.makeText(ProfileActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
        });
    }
}
