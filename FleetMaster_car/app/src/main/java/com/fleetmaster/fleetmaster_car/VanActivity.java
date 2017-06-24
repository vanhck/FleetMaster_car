package com.fleetmaster.fleetmaster_car;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Thomas on 23.06.2017.
 */

public class VanActivity  extends AppCompatActivity implements LocationListener {


    private boolean istVordertuer = false;
    private boolean istMittelkonsole = false;
    private boolean isBuchen = true;
    private Lager lager;
    private Button buchenButton;
    private Button vordertürButton;
    private Button ladetürButton;

    private NfcAdapter nfcAdapter;
    private PendingIntent nfcPendingIntent;
    public Location location;
    private static final String TAG = "NFC";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_van);
        this.lager = (Lager) this.getIntent().getExtras().get("LAGER");
        this.setTitle(this.lager.name);
        this.buchenButton = (Button) findViewById(R.id.button3);
        this.vordertürButton = (Button) findViewById(R.id.button);
        this.ladetürButton = (Button) findViewById(R.id.button2);
        this.lager = (Lager) this.getIntent().getExtras().get("LAGER");
        this.setTitle(this.lager.name);

        NfcAdapter mAdapter;
        PendingIntent mPendingIntent;
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
            //nfc not support your device.
            return;
        }
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        try {
            // initialize NFC
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        } catch (Exception e) {
            LagerActivity.displayToast(e.getMessage(), this.getApplicationContext());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        try {

            if (intent != null) {
                Parcelable[] rawMessages =
                        intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

                if (rawMessages != null) {
                    String id = new String(((NdefMessage) rawMessages[0]).getRecords()[0].getPayload()).substring(3);
                    String lagerid = this.lager.id;


                    double latitude = 49.011253 + (Math.random()*0.01)-0.02;
                    double longitude = 8.424899 + (Math.random()*0.01)-0.02;
                    TextToSpeech ttobj = null;

                    if (this.isBuchen) {
                        this.pushDataToServer(lagerid, id, longitude, latitude);
                    } else if (this.istMittelkonsole) {
                        Toast.makeText(this, "Liegt auf der Mittelkonsole",
                                Toast.LENGTH_SHORT).show();
                    } else if (this.istVordertuer) {
                        if (id.equals(this.lager.id)) {
                            Toast.makeText(this, "Door unlocked!",
                                    Toast.LENGTH_SHORT).show();
                            try {

                                final TextToSpeech finalTtobj = ttobj;
                                ttobj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                    @Override
                                    public void onInit(int status) {
                                        //finalTtobj.setLanguage(Locale.GERMANY);
                                        //finalTtobj.speak("Access granted", TextToSpeech.QUEUE_FLUSH, null);
                                    }
                                });

                            } catch (Exception e) {
                                Toast.makeText(this, e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                            LagerActivity.generateTone(400);

                        } else {
                            Toast.makeText(this, "Wrong input!  " + id + " " + this.lager.id,
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            }

        } catch (Exception e) {
            LagerActivity.displayToast("some error " + e.getMessage(), this);
            Log.d("errormessage", e.getMessage());
        }
    }

    private void pushDataToServer(String lagerid, String warenid, double latitude, double longitude) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://martinshare.com/api/van.php/registerware/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        FleetMasterServerRequest service = retrofit.create(FleetMasterServerRequest.class);

        Call<List<Objekt>> repos = service.ok(lagerid, warenid, String.valueOf(longitude), String.valueOf(latitude));
        repos.enqueue(new Callback<List<Objekt>>() {
            @Override
            public void onResponse(Call<List<Objekt>> call, Response<List<Objekt>> response) {
                if(response.body().get(0).getTyp().equals("in")) {
                    LagerActivity.generateTone(200);
                } else {
                    LagerActivity.generateTone(200);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    LagerActivity.generateTone(200);
                }
            }

            @Override
            public void onFailure(Call<List<Objekt>> call, Throwable t) {

            }
        });
        retrofit = new Retrofit.Builder()
                .baseUrl("http://martinshare.com/api/van.php/updateLagerPos/")
                .build();
        service.updateLagerPos(lagerid, String.valueOf(longitude), String.valueOf(latitude));
        Log.d("errormessage",repos.toString());
    }

    /* Request updates at startup */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "enableForegroundMode");

        // foreground mode gives the current active application priority for reading scanned tags
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED); // filter for tags
        IntentFilter[] writeTagFilters = new IntentFilter[]{tagDetected};
        nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, writeTagFilters, null);
        //locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        //locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        int lat = (int) (location.getLatitude());
        int lng = (int) (location.getLongitude());
        this.location = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }


    // Eigentlich keine buttons sondern nfc events je nach buttondruck unterschiedlicher modus -> unterschiedliche mögliche operationen
    public void buchen(View view) {
        // TODO nfc schaft schalten ob was drübergezogen wurde...
        Context context = this.getApplicationContext();
        CharSequence text = this.lager.name + " buchen";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        this.isBuchen = true;
        this.istVordertuer = false;
        this.istMittelkonsole = false;
    }
    public void autoEntsperren(View view) {
        // TODO vergleiche nfc übertragenes zeugs mit dem lager.getHash
        Context context = this.getApplicationContext();
        CharSequence text = this.lager.name + " auto entsperren";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        this.isBuchen = false;
        this.istVordertuer = true;
        this.istMittelkonsole = false;
    }
    public void mittelkonsole(View view) {
        // TODO eig tue nichts
        // endbenutzergerät muss auf verbindungsabbruch kontrollieren
        Context context = this.getApplicationContext();
        CharSequence text = this.lager.name + " mittelkonsole";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        this.isBuchen = false;
        this.istVordertuer = false;
        this.istMittelkonsole = true;
    }
}
