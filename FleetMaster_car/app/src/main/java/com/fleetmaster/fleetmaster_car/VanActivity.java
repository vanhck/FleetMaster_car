package com.fleetmaster.fleetmaster_car;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
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
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**Activity to handle the things that can be done with an Van.
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
    private WebSocketClient mWebSocketClient;
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
                    String id = new String(((NdefMessage) rawMessages[0]).getRecords()[0].getPayload());
                    Toast.makeText(this, "id " + id,
                            Toast.LENGTH_LONG).show();
                    if(id.length()>3) {
                        if (id.substring(1, 3).equals("en")) {
                            id = id.substring(3);
                        }
                    }
                    String lagerid = this.lager.id;
                    Toast.makeText(this, "id " + id,
                            Toast.LENGTH_LONG).show();

                    double latitude = 49.011253 + (Math.random()*0.01)-0.02;
                    double longitude = 8.424899 + (Math.random()*0.01)-0.02;
                    if (this.isBuchen) {
                        this.pushDataToServer(lagerid, id, longitude, latitude);
                    } else if (this.istMittelkonsole) {
                        Toast.makeText(this, "Liegt auf der Mittelkonsole",
                                Toast.LENGTH_SHORT).show();
                    } else if (this.istVordertuer) {

                            Retrofit retrofit = new Retrofit.Builder()
                                    .baseUrl("http://martinshare.com/api/van.php/checkuser/")
                                    .build();
                            FleetMasterServerRequest service = retrofit.create(FleetMasterServerRequest.class);
                            service.checkUser(id, lagerid).enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {

                                }
                            });

                            Toast.makeText(this, "Door unlocked! "   + "id " + id + " "+ lagerid,
                                    Toast.LENGTH_LONG).show();
                            boolean carTone = mWebSocketClient.getReadyState() == WebSocket.READYSTATE.OPEN;
                            if(carTone) {
                                carToneAndLight();
                            } else {
                                LagerActivity.generateTone(400);
                            }

                        } else {
                            Toast.makeText(this, "Wrong input!  " + id + " " + this.lager.id,
                                    Toast.LENGTH_LONG).show();
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
                boolean carTone = mWebSocketClient.getReadyState() == WebSocket.READYSTATE.OPEN;
                if (response.body().get(0).getTyp().equals("in")) {
                    if (carTone) {
                        carToneAndLight();
                    } else {
                        LagerActivity.generateTone(200);
                    }
                } else {
                    if (carTone) {
                        carToneAndLight();
                    } else {
                        LagerActivity.generateTone(200);
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (carTone) {
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
        retrofit = new Retrofit.Builder()
                .baseUrl("http://martinshare.com/api/van.php/updateLagerPos/")
                .build();
        service.updateLagerPos(lagerid, String.valueOf(longitude), String.valueOf(latitude));
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
        int duration = Toast.LENGTH_LONG;
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
