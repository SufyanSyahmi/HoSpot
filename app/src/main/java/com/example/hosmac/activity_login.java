package com.example.hosmac;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class activity_login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Initialize Firebase Auth & Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);

        // Login button click listener
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validate input
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(activity_login.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase authentication
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Login successful
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Fetch the user's name from Firestore using the UID
                            if (user != null) {
                                String userUID = user.getUid();
                                DocumentReference userRef = db.collection("users").document(userUID);
                                userRef.get().addOnCompleteListener(userTask -> {
                                    if (userTask.isSuccessful() && userTask.getResult().exists()) {
                                        String username = userTask.getResult().getString("name");
                                        Toast.makeText(activity_login.this, "Welcome, " + (username != null ? username : "User"), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(activity_login.this, "Welcome to HoSpot", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            // Navigate to HomeActivity after login
                            startActivity(new Intent(activity_login.this, HomeActivity.class));
                            finish();
                        } else {
                            // Capture detailed Firebase error
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(activity_login.this, "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                            Log.e("FirebaseAuth", "Login failed: " + errorMessage);
                        }
                    });
        });

        // Register button click listener
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(activity_login.this, ActivityRegister.class));
        });
    }
}
