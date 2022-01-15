package com.example.my_first_app;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.example.my_first_app.utils.StatusWatchman;
import com.example.my_first_app.utils.Utils;
import com.example.my_first_app.utils.interfaces.Listener;

public class MainActivity extends AppCompatActivity implements Listener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button button = findViewById(R.id.btn_connect);
        button.setOnClickListener(this::initiateMirroring);


        Utils.getDeviceScreenHeightAndWidthAndStoreItInStatusWatchman(this);
    }


    @SuppressLint("SetTextI18n") //we don't need internationalisation
    private void initiateMirroring(View view) {
        Log.i("info", "Initiating mirroring...");


        Log.d("debug", "Validating port an ip...");
        boolean inputsAreValid = Utils.validateEnteredPortAndIPAndStoreThemInStatusWatchmanIfValid(
                ((TextView) findViewById(R.id.txt_in_address)).getText().toString(),
                ((TextView) findViewById(R.id.txt_in_port)).getText().toString());
        if (!inputsAreValid) {
            ((TextView) findViewById(R.id.txt_errorMessage)).setText("Entered Port or Address invalid please try again!");
            Log.w("warning", "Invalid ip or port was inputted");
            Log.d("info", "Mirroring initiation stopped");
            return;
        }
        ((TextView) findViewById(R.id.txt_errorMessage)).setText("");
        Log.d("debug", "port and ip validated");



        Log.d("debug", "Creating new thread...");
        Utils.createNewThreadLaunchItAndNotifyStatusWatchman();
        Log.d("debug", "New thread created");


        Log.i("info", "Mirroring initiated");


        //listening to status watchman in order to find out if new activity should be started
        StatusWatchman.addListener(this);

        ((Button)findViewById(R.id.btn_connect)).setEnabled(false);
        ((Button)findViewById(R.id.btn_connect)).setText("Waiting");
    }


    @Override
    @SuppressLint("SetTextI18n") //we don't need internationalisation
    public void update() {
        Runnable runnable = () ->{
            if(!StatusWatchman.MIRRORING_ACTIVITY_LAUNCHED && StatusWatchman.isConnectionEstablished()){
                Log.i("info", "Creating new activity...");
                Intent intent = new Intent(this, MirroringActivity.class);
                startActivity(intent);
                Log.i("info", "New activity created");
                StatusWatchman.MIRRORING_ACTIVITY_LAUNCHED = true;
                StatusWatchman.removeListener(this);
                ((Button)findViewById(R.id.btn_connect)).setEnabled(true);
                ((Button)findViewById(R.id.btn_connect)).setText("Connect");
            }
            if(StatusWatchman.shouldConnectionBeTerminated()){//this is bad practice I guess... oh well
                ((TextView) findViewById(R.id.txt_errorMessage)).setText("Couldn't establish connection");
                ((Button)findViewById(R.id.btn_connect)).setEnabled(true);
                ((Button)findViewById(R.id.btn_connect)).setText("Connect");
            }
        };

        runOnUiThread(runnable);
    }
}



