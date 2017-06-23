package com.fleetmaster.fleetmaster_car;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by Thomas on 23.06.2017.
 */

public class LagerActivity  extends AppCompatActivity {


    private Lager lager;
    private Button buchenButton;

    private static final String TAG = "NFC";
    private NfcAdapter nfcAdapter;
    private PendingIntent nfcPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lager);
        this.lager = (Lager) this.getIntent().getExtras().get("LAGER");
        this.setTitle(this.lager.getName());
        this.buchenButton = (Button) findViewById(R.id.Buchen);

        try {
            // initialize NFC
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        } catch (Exception e) {
            LagerActivity.displayToast(e.getMessage(), this.getApplicationContext());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "enableForegroundMode");

        // foreground mode gives the current active application priority for reading scanned tags
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED); // filter for tags
        IntentFilter[] writeTagFilters = new IntentFilter[] {tagDetected};
        nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, writeTagFilters, null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.d(TAG, "onNewIntent");

        // check for NFC related actions
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            LagerActivity.displayToast("Discovered nfc tag", this);

        }

    }

    public void buchen(View view) {
        // TODO ~5s lang nfc abfragen ob was drübergezogen wurde...
        LagerActivity.displayToast("buchen", this.getApplicationContext());
    }

    public static void displayToast(String message, Context context) {
        CharSequence text = message;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    // TODO nfc scharf schalten wenn nfc event verbindung zum server aufbauen
}
