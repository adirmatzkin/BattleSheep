package com.example.user.battlesheep;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
import static com.example.user.battlesheep.LoginActivity.mAuth;
import static com.example.user.battlesheep.Menu.mDatabase;
import static com.example.user.battlesheep.Menu.stopFunction;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private Thread t;

    static Bitmap profilePic;
    static double lat;
    static double longt;
    static float maxZoom = 16.5f;
    static float circleRadius = 200;
    static MarkerOptions mo = new MarkerOptions();


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

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        lat = locationManager.getLastKnownLocation(locationManager.getAllProviders().get(0)).getLatitude();
        longt = locationManager.getLastKnownLocation(locationManager.getAllProviders().get(0)).getLongitude();

        mMap.setMyLocationEnabled(true);
        mMap.setMinZoomPreference(maxZoom);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(new LatLng(lat, longt), 16.8f)));
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

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, Menu.class).setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    @Override
    public void onDestroy()
    {
        super.onStop();
        stopFunction();
    }

    private void runThread() {

        t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!t.isInterrupted()) {
                    checkLocAvailability();
                    updateLocation();
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

    private void updateLocation()
    {

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

        if (!(locationManager.getAllProviders().size() > 0 && locationManager.getLastKnownLocation(locationManager.getAllProviders().get(0)) != null)) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMap.clear();
                Log.d("Circle: ", "ADDED");
                mMap.addCircle(new CircleOptions()
                        .clickable(false)
                        .center(new LatLng(lat, longt))
                        .radius(circleRadius)
                        .strokeWidth(2)
                        .fillColor(0x1200BBF8)
                        .strokeColor(0xFF00AAFF));
                Toast.makeText(getApplicationContext(), "Refreshed marker", Toast.LENGTH_SHORT).show();
            }
        });

        lat = locationManager.getLastKnownLocation(locationManager.getAllProviders().get(0)).getLatitude();
        longt = locationManager.getLastKnownLocation(locationManager.getAllProviders().get(0)).getLongitude();

        mDatabase.getReference().child(mAuth.getCurrentUser().getUid()).child("lat").setValue(lat + "");
        mDatabase.getReference().child(mAuth.getCurrentUser().getUid()).child("long").setValue(longt + "");

//        mMap.getMyLocation().getLatitude();
//        mMap.getMyLocation().getLongitude();
    }


    private void showFriends() {
        mDatabase.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot friends = dataSnapshot.child(mAuth.getCurrentUser().getUid()).child("Friends");
                for (DataSnapshot d : friends.getChildren()) {
                    setMarker(dataSnapshot, d);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setMarker(DataSnapshot dataSnapshot, DataSnapshot d)
    {
        DataSnapshot friend = dataSnapshot.child(d.getValue().toString());

        //Friend is online?
        if(!friend.hasChild("Active"))
            return;

        if (friend.hasChild("lat") && friend.hasChild("long") && friend.child("Active").getValue().toString().equals("True")) {
            final double lat = Double.parseDouble(friend.child("lat").getValue().toString());
            final double longt = Double.parseDouble(friend.child("long").getValue().toString());

            ImageR imageR = new ImageR(lat, longt, profilePicUrl(friend.child("ID").getValue().toString()));
            Thread image = new Thread(imageR);
            image.start();
            try {
                //Wait for the thread to finish running
                image.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mo.title(friend.child("Name").getValue().toString());

            //Add marker to map
            addMarkerToMap(mo);
        }
    }

    private void addMarkerToMap(final MarkerOptions mo)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMap.addMarker(mo);
            }
        });
    }

    //Return the profile picture url by the facebook id.
    private String profilePicUrl(String facebookID)
    {
        return "https://graph.facebook.com/" + facebookID + "/picture?type=large";
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
