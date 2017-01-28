package com.example.user.battlesheep;

import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
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

public class Menu extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, android.view.Menu {

    private static final String TAG = Menu.class.getSimpleName();

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private FirebaseDatabase mDatabase;

    private TextView navMail;

    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        sharedPref = getApplicationContext().getSharedPreferences("com.example.myapp.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Should do something...?", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ////////////////////////////////////////////////////// Set name in database
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Toast.makeText(getApplicationContext(), "onAuthStateChanged:signed_in:" + user.getUid(), Toast.LENGTH_SHORT).show();
                } else {
                    // User is signed out
                    System.out.print("onAuthStateChanged:signed_out");
                }
            }
        };

        if(mAuth.getCurrentUser() == null)// For some FUCKING reason thats the only way to fix the authentication.
        {
            startActivity(new Intent(Menu.this, Menu.class));
        }

        if(isFacebookLoggedIn())
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
        else
        {
//            navMail.setText(mAuth.getCurrentUser().getEmail());
        }
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
            startActivity(new Intent(Menu.this, MapsActivity.class));
        } else if (id == R.id.nav_nfc_layout) {
//            fragmentManager.beginTransaction()
//                    .replace(R.id.content_frame
//                            , new NFC_Activity_Fragment())
//                    .commit();
            startActivity(new Intent(this, NFC_Activity_Fragment.class));

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
            startActivity(new Intent(Menu.this, LoginActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
                if (!mAuth.getCurrentUser().getUid().equals(keyOfUserToAdd)) { // to make sure we don't add ourselves as a friend...

                    //Check if the friend is already in the friends list.
                    boolean hasFriend = false;

                    for (DataSnapshot aFriend : dataSnapshot.child(mAuth.getCurrentUser().getUid()).child("Friends").getChildren()) {
                        if (aFriend.getValue().toString().equals(keyOfUserToAdd)) {
                            hasFriend = true; // he is already a friend of mine
                            Toast.makeText(Menu.this, "This user is already your friend", Toast.LENGTH_SHORT).show();
                        }
                    }

                    //Add the friend if its ok.
                    if (!hasFriend) {
                        friendsChild.child(String.valueOf(dataSnapshot.child(mAuth.getCurrentUser().getUid()).child("Friends").getChildrenCount() + 1)).setValue(keyOfUserToAdd);
                        Toast.makeText(Menu.this, "Successfully added a friend!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
