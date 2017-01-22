package com.example.user.battlesheep;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebase;
    private LocationManager locationManager;

    Thread t;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
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

    private void runThread() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                while (true)
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                Criteria c = new Criteria();
                                c.setAccuracy(Criteria.ACCURACY_LOW);
                                c.setAltitudeRequired(false);
                                c.setBearingRequired(false);
                                c.setCostAllowed(false);
                                c.setPowerRequirement(Criteria.NO_REQUIREMENT);
                                c.setSpeedRequired(false);

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
                                if(locationManager.getAllProviders().size() > 0 && locationManager.getLastKnownLocation(locationManager.getAllProviders().get(0)) != null) {
                                    double lat = locationManager.getLastKnownLocation(locationManager.getAllProviders().get(0)).getLatitude();
                                    double longt = locationManager.getLastKnownLocation(locationManager.getAllProviders().get(0)).getLongitude();

                                    mFirebase.getReference().child(mAuth.getCurrentUser().getUid()).child("lat").setValue(lat + "");
                                    mFirebase.getReference().child(mAuth.getCurrentUser().getUid()).child("long").setValue(longt + "");

                                    LatLng myLoc = new LatLng(lat, longt);
                                    mMap.clear();
                                    mMap.addMarker(new MarkerOptions().position(myLoc).title("My location"));
                                    Toast.makeText(getApplicationContext(), "Refreshed marker", Toast.LENGTH_SHORT).show();
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLoc));
                                }
                            }
                        }

                    });

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        Toast.makeText(getApplicationContext(), "Cant sleep", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        };
        t = new Thread(r);
        t.start();
    }
}
