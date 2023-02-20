package com.racecontrolapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private dataSender dataSender;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private Button startButton;
    private Button stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editText = findViewById(R.id.compNumber);
        stopButton = findViewById(R.id.compStop);
        stopButton.setEnabled(false);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRacing();
            }
        });

        startButton = findViewById(R.id.compStart);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                if (text.equals("")) {
                    Toast.makeText(MainActivity.this, "הכנס מספר משתתף", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Do something with the text from the EditText
                dataSender = new dataSender(MainActivity.this, Integer.parseInt(text));
                // Permissions
                if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // Permission is already granted, start the GPSLocationSender
                    startRacing();
                } else {
                    // Permission has not been granted, request it from the user
                    ActivityCompat.requestPermissions(MainActivity.this , new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, start the GPSLocationSender
                startRacing();
            } else {
                // Permission was denied, show an error message or take some other action
                Toast.makeText(MainActivity.this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void startRacing() {
        dataSender.start();
        stopButton.setEnabled(true);
        startButton.setEnabled(false);
    }

    public void stopRacing() {
        dataSender.stop();
        stopButton.setEnabled(false);
        startButton.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataSender.stop();
    }
}



