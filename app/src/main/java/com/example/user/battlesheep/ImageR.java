package com.example.user.battlesheep;

import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.example.user.battlesheep.MapsActivity.profilePic;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Raziel on 1/28/2017.
 */

public class ImageR implements Runnable {
    double lat, longt;
    MarkerOptions mo = new MarkerOptions();
    String source;

    public ImageR(double lat, double longt, String source)
    {
        this.lat = lat;
        this.longt = longt;

        this.source = source;
    }

    @Override
    public void run() {
        MapsActivity.getBitmapFromURL(source);

        mo.icon(BitmapDescriptorFactory.fromBitmap(profilePic));

        if(mo.getIcon() == null)
            Toast.makeText(getApplicationContext(), "Null image on goo", Toast.LENGTH_SHORT).show();

        LatLng myLoc = new LatLng(lat, longt);
        mo.position(myLoc);
        MapsActivity.mo = mo;
    }
}
