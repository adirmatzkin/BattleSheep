package com.example.user.battlesheep;

import android.graphics.Bitmap;
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

class ImageR implements Runnable {
    private double lat, longt;
    private MarkerOptions mo = new MarkerOptions();
    private String source;

    public ImageR(double lat, double longt, String source)
    {
        this.lat = lat;
        this.longt = longt;
        this.source = source;
    }

    @Override
    public void run() {
        MapsActivity.getBitmapFromURL(source);
        setMo(profilePic, lat, longt);
        MapsActivity.mo = mo;
    }

    private void setMo(Bitmap bitmap, double lat, double longt)
    {
        mo.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        if(mo.getIcon() == null)
            Toast.makeText(getApplicationContext(), "Null image on goo", Toast.LENGTH_SHORT).show();

        LatLng myLoc = new LatLng(lat, longt);
        mo.position(myLoc);
    }
}
