package com.fleetmaster.fleetmaster_car;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Activity which displays things you can do with a storage system. Checking things out or in.
 * Created by Thomas on 23.06.2017.
 */

public class LagerActivity extends AppCompatActivity implements LocationListener {


    private Lager lager;
    private Button buchenButton;

    private static final String TAG = "NFC";
    private NfcAdapter nfcAdapter;
    private PendingIntent nfcPendingIntent;
    public Location location;
    private boolean buzzing = false;

    private LocationManager locationManager;
    private String provider;

    private WebSocketClient mWebSocketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lager);
        this.lager = (Lager) this.getIntent().getExtras().get("LAGER");
        this.setTitle(this.lager.name);
        this.buchenButton = (Button) findViewById(R.id.Buchen);

        try {
            // initialize NFC
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            // initialize websocket
            connectWebSocket();
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
                    String warenid = new String(((NdefMessage) rawMessages[0]).getRecords()[0].getPayload()).substring(3);
                    String lagerid = this.lager.id;


                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

                    double latitude = 49.011253 + (Math.random()*0.01)-0.02;
                    double longitude = 8.424899 + (Math.random()*0.01)-0.02;
                    try {
                        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        // Define the criteria how to select the locatioin provider -> use
                        // default
                        Criteria criteria = new Criteria();
                        provider = locationManager.getBestProvider(criteria, false);
                        Location location = locationManager.getLastKnownLocation(provider);

                        // Initialize the location fields
                        if (location != null) {
                            System.out.println("Provider " + provider + " has been selected.");
                            onLocationChanged(location);
                        }
                        //latitude = location.getLatitude();
                        //longitude = location.getLongitude();
                    } catch (Exception e) {
                        Log.e("error", e.getMessage());
                    }



                    URL url = new URL("http://martinshare.com/api/van.php/registerware/" + lagerid + "/"  + warenid + "/" + longitude + "/" + latitude);
                    this.pushDataToServer(lagerid, warenid, longitude, latitude);


                    //http://martinshare.com/api/van.php/registerware/%7Blagerid%7D/%7Bwarenid%7D/%7Blon%7D/%7Blat%7D
                    LagerActivity.displayToast(warenid, this);
                }

            }
        } catch (Exception e) {
            LagerActivity.displayToast("some error " + e.getMessage(), this);
            Log.d("errormessage",e.getMessage());
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
                boolean carTone = mWebSocketClient.getReadyState() == WebSocket.READYSTATE.OPEN;
                if(response.body().get(0).getTyp().equals("in")) {
                    if(carTone) {
                        carToneAndLight();
                    } else {
                        LagerActivity.generateTone(200);
                    }
                } else {
                    if(carTone) {
                        carToneAndLight();
                    } else {
                        LagerActivity.generateTone(200);
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(carTone) {
                        carToneAndLight();
                    } else {
                        LagerActivity.generateTone(200);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Objekt>> call, Throwable t) {

            }
        });
        Log.d("errormessage",repos.toString());

    }

    public void carToneAndLight()  {
        this.startCarTone();
        this.startCarLight();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.stopCarTone();
        this.stopCarLight();
    }

    private void startCarLight() {
        try {
            this.sendMessage("{\"action\":\"Set\", \"path\":\"Signal.Body.Lights.IsHighBeamOn\", \"value\":\"true\", \"requestId\":\"af6b2f9e-d7ca-461c-95d4-2c52078e4b57\"}");
        } catch (Exception e) {
            Toast.makeText(this, "Exception" + e.getMessage() + " " + (this.mWebSocketClient!=null),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void stopCarLight() {
        try {
            this.sendMessage("{\"action\":\"Set\", \"path\":\"Signal.Body.Lights.IsHighBeamOn\", \"value\":\"false\", \"requestId\":\"af6b2f9e-d7ca-461c-95d4-2c52078e4b57\"}");
        } catch (Exception e) {
            Toast.makeText(this, "Exception" + e.getMessage() + " " + (this.mWebSocketClient!=null),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void startCarTone() {
        try {
            this.sendMessage("{\"action\":\"Set\", \"path\":\"Signal.Cabin.Buzzer\", \"value\":\"true\", \"requestId\":\"af6b2f9e-d7ca-461c-95d4-2c52078e4b55\"}");
        } catch (Exception e) {
            Toast.makeText(this, "Exception" + e.getMessage() + " " + (this.mWebSocketClient!=null),
                    Toast.LENGTH_SHORT).show();
        }
    }
    private void stopCarTone() {
        try {
            this.sendMessage("{\"action\":\"Set\", \"path\":\"Signal.Cabin.Buzzer\", \"value\":\"false\", \"requestId\":\"af6b2f9e-d7ca-461c-95d4-2c52078e4b56\"}");
        } catch (Exception e) {
            Toast.makeText(this, "Exception" + e.getMessage() + " " + (this.mWebSocketClient!=null),
                    Toast.LENGTH_SHORT).show();
        }
    }
    public static void generateTone(int duration) {
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, duration); // 200 is duration in ms

    }

    public void buchen(View view) {
        LagerActivity.displayToast("buchen", this.getApplicationContext());

    }

    public static void displayToast(String message, Context context) {
        CharSequence text = message;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
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

    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://172.16.0.1:4443/websocket");
        } catch (URISyntaxException e) {
            Toast.makeText(this, e.getMessage(),
                    Toast.LENGTH_LONG).show();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }

    public void sendMessage(String message) {
        mWebSocketClient.send(message);

    }



}
