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

    private PowerManager.WakeLock wakeLock;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editText = findViewById(R.id.compNumber);
        Button button = findViewById(R.id.compStart);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataSender.stop();
    }
}



