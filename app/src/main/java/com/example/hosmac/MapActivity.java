package com.example.hosmac;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView hospitalName, hospitalAddress;
    private View infoContainer;
    private FirebaseAuth mAuth;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Remove the default back button in the ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false); // Disable the default back arrow
            actionBar.setDisplayShowTitleEnabled(false); // Hide the default title if needed
        }

        // Custom back button click listener (the one in ImageView)
        ImageView backButton = findViewById(R.id.back_button);  // Make sure the back button ID matches
        backButton.setOnClickListener(v -> {
            onBackPressed();  // Go back to the previous activity
        });

        // Initialize Firebase Auth and Firebase Realtime Database
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Initialize UI elements
        hospitalName = findViewById(R.id.location_name);
        hospitalAddress = findViewById(R.id.location_description);
        infoContainer = findViewById(R.id.location_details);

        // Load the Google Map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f));

                                // Save user's location in Firebase Realtime Database
                                saveUserLocationToDatabase(location);
                            } else {
                                Toast.makeText(MapActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // Add hospital markers with addresses
        addHospitalMarker(new LatLng(5.766759, 102.240143), "Hospital Machang, Malaysia", "Jalan Hospital, 18500 Machang, Kelantan");
        addHospitalMarker(new LatLng(5.898342, 102.160533), "Hospital Tanah Merah, Malaysia", "Jalan Hospital, 17500 Tanah Merah, Kelantan");
        addHospitalMarker(new LatLng(6.051417, 102.154222), "Hospital Pasir Mas, Malaysia", "Jalan Hospital, 17000 Pasir Mas, Kelantan");
        addHospitalMarker(new LatLng(5.831090, 102.382377), "Hospital Tengku Anis, Pasir Puteh, Malaysia", "Jalan Hospital, 16800 Pasir Puteh, Kelantan");
        addHospitalMarker(new LatLng(6.131325, 102.292835), "Hospital Universiti Sains Malaysia, Kota Bharu", "Kubang Kerian, 16150 Kota Bharu, Kelantan");

        // Move camera to focus on hospitals in Kelantan
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(5.766759, 102.240143), 10f));

        // Handle marker clicks
        mMap.setOnMarkerClickListener(marker -> {
            displayHospitalInfo(marker);
            return false;
        });
    }

    private void addHospitalMarker(LatLng location, String title, String address) {
        mMap.addMarker(new MarkerOptions()
                .position(location)
                .title(title)
                .snippet(address) // This will show the address in the marker info window
        );
    }

    private void saveUserLocationToDatabase(Location location) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();  // Get the current user's UID

            // Save the user's location data under their UID in Firebase Realtime Database
            DatabaseReference userLocationRef = FirebaseDatabase.getInstance().getReference("user_locations").child(userId);

            // Create a map to store the location data
            HashMap<String, Object> locationData = new HashMap<>();
            locationData.put("latitude", location.getLatitude());
            locationData.put("longitude", location.getLongitude());
            locationData.put("timestamp", System.currentTimeMillis());  // Add timestamp for reference

            // Store the location data in Firebase
            userLocationRef.setValue(locationData).addOnSuccessListener(aVoid -> {
                Toast.makeText(MapActivity.this, "User location saved successfully", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(MapActivity.this, "Failed to save location", Toast.LENGTH_SHORT).show();
            });
        } else {
            // If no user is logged in
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayHospitalInfo(Marker marker) {
        String title = marker.getTitle();
        String address = marker.getSnippet(); // Ambil alamat dari snippet

        hospitalName.setText(title);
        hospitalAddress.setText(address != null ? address : "Address not available");

        infoContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Handle About Us option
        if (id == R.id.action_about) {
            Intent aboutIntent = new Intent(MapActivity.this, AboutActivity.class);
            startActivity(aboutIntent);
            return true;
        }
        // Handle Profile option
        else if (id == R.id.action_profile) {
            Intent profileIntent = new Intent(MapActivity.this, ProfileActivity.class);
            startActivity(profileIntent); // Corrected the variable name to profileIntent
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onMapReady(mMap);
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
