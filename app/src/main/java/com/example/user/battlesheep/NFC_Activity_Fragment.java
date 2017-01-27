package com.example.user.battlesheep;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by adirmatzkin on 24/01/2017.
 */

public class NFC_Activity_Fragment extends Activity implements
        NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {

    TextView textInfo;
    EditText textOut;
    FirebaseAuth mAuth;

    NfcAdapter nfcAdapter;

    private FirebaseDatabase db;
    private static final String TAG = NFC_Activity_Fragment.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_activity);
        textInfo = (TextView) findViewById(R.id.info);
        textOut = (EditText) findViewById(R.id.textout);

        db = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // check if nfc is available
        if (nfcAdapter == null) {
            // no nfc - finish
            Toast.makeText(this, "No NFC adapter exists",
                    Toast.LENGTH_LONG).show();
            finish();
        } else if (!nfcAdapter.isEnabled()) {
            // nfc is on - open settings
            Toast.makeText(this, "Turn on NFC", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
            while (!nfcAdapter.isEnabled()) {
                // wait for user to turn nfc on
            }
            Toast.makeText(this, "NFC turned on...", Toast.LENGTH_SHORT).show();
            // maybe add here an automatic start of the activity
        } else {
            // nfc is on - keep going
            Toast.makeText(this, "Ready to use NFC", Toast.LENGTH_LONG).show();
            String idToSend = mAuth.getCurrentUser().getUid().toString();
            Log.d(TAG, "--------------------------------------"+idToSend.toString());
            nfcAdapter.setNdefPushMessageCallback(this, this);
            nfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String action = intent.getAction();
        if(action != null && action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){
            Parcelable[] parcelables =
                    intent.getParcelableArrayExtra(
                            NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage inNdefMessage = (NdefMessage)parcelables[0];
            NdefRecord[] inNdefRecords = inNdefMessage.getRecords();
            NdefRecord NdefRecord_0 = inNdefRecords[0];
            String inMsg = new String(NdefRecord_0.getPayload());

            //inMsg == the id got from the other device.
            // add to friends
            addUidByIDToFriends(inMsg, db.getReference().child(mAuth.getCurrentUser().getUid()).child("Friends"));
            // show some info on screen
            textInfo.setText("Got \""+inMsg+"\" from your friends device\nNow send him yours (\""+mAuth.getCurrentUser().getUid().toString()+"\")");


            // need to change this so that the id's will be switched not as 2 separate beam action, but as one beam & respond.

        }
    }



    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {

        final String eventString = "onNdefPushComplete\n" + event.toString();
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        eventString,
                        Toast.LENGTH_LONG).show();
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {

        String idToSend = mAuth.getCurrentUser().getUid().toString();
        byte[] bytesOut = idToSend.getBytes();

        NdefRecord ndefRecordOut = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA,
                "text/plain".getBytes(),
                new byte[] {},
                bytesOut);

        NdefMessage ndefMessageOut = new NdefMessage(ndefRecordOut);
        return ndefMessageOut;
    }




    //Add a user Firebase id to a given database reference by his Facebook ID.
    public void addUidByIDToFriends(final String idToAdd, final DatabaseReference friendsChild)
    {
        db.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Loop to check which user has the given ID.
                for(DataSnapshot d : dataSnapshot.getChildren())
                {
                    if(d.hasChild("ID"))
                    {

                        //If the user has that ID:
                        if(d.child("ID").getValue().toString().equals(idToAdd))
                        {
                            Log.d(TAG, "got hereeeeeeeeeeeeeeeeeeeeeee");

                            boolean hasFriend = false;
                            //Check if friend is already in the friends list.
                            for(DataSnapshot ds : dataSnapshot.child(mAuth.getCurrentUser().getUid()).child("Friends").getChildren())
                            {
                                if(ds.getValue().toString().equals(d.getKey()))
                                {
                                    hasFriend = true;
                                }
                            }
                            //Adds the friend to the list.
                            if(!hasFriend)
                                friendsChild.child(String.valueOf(dataSnapshot.child(mAuth.getCurrentUser().getUid()).child("Friends").getChildrenCount() + 1)).setValue(d.getKey());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }






}