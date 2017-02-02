package com.example.user.battlesheep;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Debug;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {


    private static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebase;
    private LocationManager locationManager;

    static Bitmap profilePic;

    static MarkerOptions mo = new MarkerOptions();
    private Thread t;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        // check if network and location are available
        checkLocAvailability();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        mFirebase = FirebaseDatabase.getInstance();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                t.start();
            }

            @Override
            public void onProviderDisabled(String provider) {
                t.stop();
            }
        };
    }


    // makes sure internet connection and location services are available
    public void checkLocAvailability() {

        LocationManager lmLoc = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = lmLoc.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gps_enabled) {
            //Toast.makeText(this, "You need to turn on location services!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            while (!gps_enabled) {
                // wait for user to turn location services on
                gps_enabled = lmLoc.isProviderEnabled(LocationManager.GPS_PROVIDER);
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        runThread();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (t != null && t.isInterrupted())
            t.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (t != null)
            t.interrupt();
    }

    private void runThread() {

        t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!t.isInterrupted()) {
                    checkLocAvailability();
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                                Criteria c = new Criteria();
//                                c.setAccuracy(Criteria.ACCURACY_LOW);
//                                c.setAltitudeRequired(false);
//                                c.setBearingRequired(false);
//                                c.setCostAllowed(false);
//                                c.setPowerRequirement(Criteria.NO_REQUIREMENT);
//                                c.setSpeedRequired(false);

                        if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        if (locationManager.getAllProviders().size() > 0 && locationManager.getLastKnownLocation(locationManager.getAllProviders().get(0)) != null) {


                            double lat = locationManager.getLastKnownLocation(locationManager.getAllProviders().get(0)).getLatitude();
                            double longt = locationManager.getLastKnownLocation(locationManager.getAllProviders().get(0)).getLongitude();

                            mFirebase.getReference().child(mAuth.getCurrentUser().getUid()).child("lat").setValue(lat + "");
                            mFirebase.getReference().child(mAuth.getCurrentUser().getUid()).child("long").setValue(longt + "");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Refreshed marker", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    showFriends();

                    try {
                        Thread.sleep(10000);
                        // check if network and location are available
                        checkLocAvailability();
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        t.start();
    }

    private void showFriends() {
        mFirebase.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot friends = dataSnapshot.child(mAuth.getCurrentUser().getUid()).child("Friends");
                for (DataSnapshot d : friends.getChildren()) {
                    DataSnapshot friend = dataSnapshot.child(d.getValue().toString());
                    if (friend.hasChild("lat") && friend.hasChild("long")) {
                        final double lat = Double.parseDouble(friend.child("lat").getValue().toString());
                        final double longt = Double.parseDouble(friend.child("long").getValue().toString());

                        ImageR imageR = new ImageR(lat, longt, "https://graph.facebook.com/" + friend.child("ID").getValue().toString() + "/picture?type=large");
                        Thread image = new Thread(imageR);
                        image.start();
                        try {
                            image.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mo.title(friend.child("Name").getValue().toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mMap.addMarker(mo);
                                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return;
                                }
                                mMap.setMyLocationEnabled(true);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MapsActivity.this, "Permission denied to te GPS location", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public static void getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            profilePic = BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            profilePic = null;
        }
    }
}
