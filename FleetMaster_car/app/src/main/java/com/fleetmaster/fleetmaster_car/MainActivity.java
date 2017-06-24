package com.fleetmaster.fleetmaster_car;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private String serverAnswer = "";
    private static TextView displayAnswer;
    private static List<Lager> lagerListe = new ArrayList<Lager>();
    private static LinearLayout buttonContainer;
    private static Context appContext;
    private static List<Button> buttons = new ArrayList<Button>();
    private static View.OnClickListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity.appContext = this.getApplicationContext();
        this.listener = new ButtonListener(this);
        this.displayAnswer = (TextView)this.findViewById(R.id.textView);
        this.buttonContainer = (LinearLayout) this.findViewById(R.id.buttonContainer);
        this.displayServerAnswer();

    }

    private void displayServerAnswer() {
        try {
            this.displayAnswer.setText("Loading data from Server");

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://martinshare.com/api/van.php/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            FleetMasterServerRequest service = retrofit.create(FleetMasterServerRequest.class);

            Call<List<Lager>> repos = service.listRepos();
            repos.enqueue(new Callback<List<Lager>>() {

                @Override
                public void onResponse(Call<List<Lager>> call, Response<List<Lager>> response) {
                    for(Lager lager : response.body()) {
                        MainActivity.lagerListe.add(lager);
                        Button button = new Button(MainActivity.appContext);
                        button.setText(lager.name);
                        button.setOnClickListener(MainActivity.listener);
                        MainActivity.buttonContainer.addView(button);
                    }


                }

                @Override
                public void onFailure(Call<List<Lager>> call, Throwable t) {

                }
            });


        }
        catch(Exception e) {
            MainActivity.displayAnswer.setText(e.getMessage());
        }
    }

    class ButtonListener implements View.OnClickListener {

        private MainActivity mainActivity;

        public ButtonListener(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void onClick(View v) {
            Button buttonClicked = (Button) v ;
            for(Lager lager : this.mainActivity.lagerListe) {
                if(buttonClicked.getText().equals(lager.name)) {
                    if(lager.typ.equals("Van")) {
                        Intent intent = new Intent(this.mainActivity, VanActivity.class);
                        intent.putExtra("LAGER", lager);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(this.mainActivity, LagerActivity.class);
                        intent.putExtra("LAGER", lager);
                        startActivity(intent);
                    }

                }
            }
        }
    }
    }
