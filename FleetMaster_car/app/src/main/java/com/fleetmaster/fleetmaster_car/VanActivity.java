package com.fleetmaster.fleetmaster_car;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by Thomas on 23.06.2017.
 */

public class VanActivity  extends AppCompatActivity {


    private Lager lager;
    private Button buchenButton;
    private Button vordertürButton;
    private Button ladetürButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_van);
        this.lager = (Lager) this.getIntent().getExtras().get("LAGER");
        this.setTitle(this.lager.getName());
        this.buchenButton = (Button) findViewById(R.id.button3);
        this.vordertürButton = (Button) findViewById(R.id.button);
        this.ladetürButton = (Button) findViewById(R.id.button2);

        NfcAdapter mAdapter;
        PendingIntent mPendingIntent;
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
            //nfc not support your device.
            return;
        }
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    // Eigentlich keine buttons sondern nfc events je nach buttondruck unterschiedlicher modus -> unterschiedliche mögliche operationen
    public void buchen(View view) {
        // TODO nfc schaft schalten ob was drübergezogen wurde...
        Context context = this.getApplicationContext();
        CharSequence text = this.lager.getName() + " buchen";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
    public void autoEntsperren(View view) {
        // TODO vergleiche nfc übertragenes zeugs mit dem lager.getHash
        Context context = this.getApplicationContext();
        CharSequence text = this.lager.getName() + " auto entsperren";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
    public void mittelkonsole(View view) {
        // TODO eig tue nichts
        // endbenutzergerät muss auf verbindungsabbruch kontrollieren
        Context context = this.getApplicationContext();
        CharSequence text = this.lager.getName() + " mittelkonsole";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
