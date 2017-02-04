package com.example.user.battlesheep;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
import static com.example.user.battlesheep.LoginActivity.mAuth;
import static com.example.user.battlesheep.Menu.setActive;

public class SettingsActivity extends AppCompatActivity {

    static String KEY_PREF_SHOW_LOCATION = "pref_show_location";
    static SharedPreferences prefs;

    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PreferenceF())
                .commit();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        setPreferenceListener();
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, Menu.class).setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private void setPreferenceListener()
    {
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if(key.equals(KEY_PREF_SHOW_LOCATION))
                {
                    setActive();
                }
            }
        };

        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent intent = new Intent(this, Menu.class).setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAuth.getCurrentUser() == null)
            return;
    }
}
