package com.example.user.battlesheep;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Raziel on 2/1/2017.
 */

public class PreferenceF extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences_main);
    }
}
