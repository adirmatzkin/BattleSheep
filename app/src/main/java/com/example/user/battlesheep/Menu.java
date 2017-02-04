package com.example.user.battlesheep;

import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
import static com.example.user.battlesheep.LoginActivity.mAuth;

public class Menu extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, android.view.Menu {

    static SharedPreferences sharedPref;
    static SharedPreferences.Editor editor;
    static FirebaseDatabase mDatabase;
    static DatabaseReference myData;

    private TextView navMail;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String name;

    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        myData = mDatabase.getReference().child(mAuth.getCurrentUser().getUid());

        sharedPref = getApplicationContext().getSharedPreferences("com.example.myapp.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        makeActionOverflowMenuShown();

        setFab();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mDatabase.getReference().child(mAuth.getCurrentUser().getUid()).child("Active").setValue("True");

        if(isFacebookLoggedIn())
        {
            getFacebookInformation();
        }

        setActive();
    }

    private void getFacebookInformation()
    {
        name = "";

        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        // Application code
                        try {
                            if(mAuth.getCurrentUser() != null)
                            {
                                //Sets the user's Facebook's id
                                mDatabase.getReference().child(mAuth.getCurrentUser().getUid()).child("ID").setValue(object.getString("id"));
                                //Gets the user's name and sets it in the nav menu
                                String email = object.getString("email");
                                navMail = (TextView) findViewById(R.id.navMail);
                                navMail.setText(email);
                                //Gets the friends array
                                JSONArray friendsList = response.getJSONObject().getJSONObject("friends").getJSONArray("data");
                                for(int i = 0; i < friendsList.length(); i++)
                                {
                                    //Adds the uid of the friend to the friends list by his Facebook id.
                                    addUidByIDToFriends(friendsList.getJSONObject(i).getString("id"), mDatabase.getReference().child(mAuth.getCurrentUser().getUid()).child("Friends"));
                                }
                                //Sets the user's name in the database.
                                name = response.getJSONObject().getString("name");
                                mDatabase.getReference().child(mAuth.getCurrentUser().getUid()).child("Name").setValue(name);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email, friends");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void setFab()
    {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Should do something...?", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

//    //@Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentManager fragmentManager = getFragmentManager();

        if (id == R.id.nav_whos_layout) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame
                            , new WhosAroundFragment())
                    .commit();
        } else if (id == R.id.nav_map_layout) {

            startActivity(new Intent(Menu.this, MapsActivity.class).setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT));
        } else if (id == R.id.nav_nfc_layout) {
//            fragmentManager.beginTransaction()
//                    .replace(R.id.content_frame
//                            , new NFC_Activity_Fragment())
//                    .commit();
            startActivity(new Intent(this, NFC_Activity_Fragment.class).setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT));

        } else if (id == R.id.nav_share) {
            // do nothing for now..
        } else if (id == R.id.nav_logout) {
            editor.putString("Email", "");
            editor.putString("Password", "");
            editor.putBoolean("Remember", false);
            editor.commit();
            if(isFacebookLoggedIn())
            {
                LoginManager.getInstance().logOut();
            }
            mAuth.signOut();
            startActivity(new Intent(Menu.this, LoginActivity.class).setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setActive();
    }

    @Override
    public void onDestroy()
    {
        super.onStop();
        stopFunction();
    }

    public static void stopFunction()
    {
        myData.child("Active").setValue("False");
    }

    public static void setActive()
    {
        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SettingsActivity.KEY_PREF_SHOW_LOCATION, true))
        {
            myData.child("Active").setValue("True");
        }
        else
        {
            myData.child("Active").setValue("False");
        }
    }

    @Override
    public MenuItem add(CharSequence title) {
        return null;
    }

    @Override
    public MenuItem add(int titleRes) {
        return null;
    }

    @Override
    public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
        return null;
    }

    @Override
    public MenuItem add(int groupId, int itemId, int order, int titleRes) {
        return null;
    }

    @Override
    public SubMenu addSubMenu(CharSequence title) {
        return null;
    }

    @Override
    public SubMenu addSubMenu(int titleRes) {
        return null;
    }

    @Override
    public SubMenu addSubMenu(int groupId, int itemId, int order, CharSequence title) {
        return null;
    }

    @Override
    public SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) {
        return null;
    }

    @Override
    public int addIntentOptions(int groupId, int itemId, int order, ComponentName caller, Intent[] specifics, Intent intent, int flags, MenuItem[] outSpecificItems) {
        return 0;
    }

    @Override
    public void removeItem(int id) {

    }

    @Override
    public void removeGroup(int groupId) {

    }

    @Override
    public void clear() {

    }

    @Override
    public void setGroupCheckable(int group, boolean checkable, boolean exclusive) {

    }

    @Override
    public void setGroupVisible(int group, boolean visible) {

    }

    @Override
    public void setGroupEnabled(int group, boolean enabled) {

    }

    @Override
    public boolean hasVisibleItems() {
        return false;
    }

    @Override
    public MenuItem findItem(int id) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public MenuItem getItem(int index) {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
        return false;
    }

    @Override
    public boolean isShortcutKey(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean performIdentifierAction(int id, int flags) {
        return false;
    }

    @Override
    public void setQwertyMode(boolean isQwerty) {

    }

    public boolean isFacebookLoggedIn(){
        return AccessToken.getCurrentAccessToken() != null;
    }


    //Add a user Firebase id to a given database reference by his ID.
    public void addUidByIDToFriends(final String keyOfUserToAdd, final DatabaseReference friendsChild)
    {

        mDatabase.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //If the user has that ID:
                if(mAuth.getCurrentUser() == null)
                {
                    return;
                }

                if (!mAuth.getCurrentUser().getUid().equals(keyOfUserToAdd)) { // to make sure we don't add ourselves as a friend...

                    //Check if the friend is already in the friends list.
                    boolean hasFriend = false;

                    for (DataSnapshot aFriend : dataSnapshot.child(mAuth.getCurrentUser().getUid()).child("Friends").getChildren()) {
                        if (aFriend.getValue().toString().equals(keyOfUserToAdd)) {
                            hasFriend = true; // he is already a friend of mine
                        }
                    }

                    //Add the friend if its ok.
                    if (!hasFriend) {
                        friendsChild.child(String.valueOf(dataSnapshot.child(mAuth.getCurrentUser().getUid()).child("Friends").getChildrenCount() + 1)).setValue(keyOfUserToAdd);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_settings:
            {
                Intent intent = new Intent(this, SettingsActivity.class).setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void makeActionOverflowMenuShown() {
        //devices with hardware menu button (e.g. Samsung Note) don't show action overflow menu
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
        }
    }

}
