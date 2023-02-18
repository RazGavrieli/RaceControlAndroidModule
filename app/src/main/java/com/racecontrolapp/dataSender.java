package com.racecontrolapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class dataSender implements LocationListener {
    private static final int UPDATE_INTERVAL = 250; // in milliseconds
    private Context context;
    private LocationManager locationManager;
    private Handler handler;
    private Runnable sendLocationRunnable;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;

    private double gforceValue = -1;
    private int compNumber;

    //private String url = "http://10.0.2.2:8000/";
    private String url = "http://192.168.1.111:8000/";


    public dataSender(Context context, int compNumber) {
        this.compNumber = compNumber;
        this.context = context;

        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // Create a handler and a runnable to send location updates to the server
        this.handler = new Handler();

        // Get the g-force data from the accelerometer sensor
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    float xg = event.values[0] / SensorManager.GRAVITY_EARTH;
                    float yg = event.values[1] / SensorManager.GRAVITY_EARTH;
                    float zg = event.values[2] / SensorManager.GRAVITY_EARTH;
                    double overallGforce = Math.sqrt(xg*xg + yg*yg + zg*zg);
                    gforceValue = Math.max(gforceValue, overallGforce); // save biggest g force
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Do nothing
            }
        }, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void start() {
        // Request location updates
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_INTERVAL, 0, this);
        }
        //sensorManager.registerListener((SensorEventListener) this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        this.sendLocationRunnable = new Runnable() {
            @Override
            public void run() {
                sendLocationToServer();
                handler.postDelayed(sendLocationRunnable, UPDATE_INTERVAL);
            }
        };
        handler.postDelayed(sendLocationRunnable, UPDATE_INTERVAL);
    }

    public void stop() {
        // Stop location updates
        locationManager.removeUpdates(this);
        //sensorManager.unregisterListener((SensorListener) this);

        // Stop sending location updates to the server
        handler.removeCallbacks(sendLocationRunnable);
    }

    @Override
    public void onLocationChanged(Location location) {
        // Do something with the new location, e.g. update a variable
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Handle status changes, if necessary
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Handle provider enabled, if necessary
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Handle provider disabled, if necessary
    }


    private void sendLocationToServer() {
        // Get the last known location from the location manager
        @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location != null) {
            // Get the latitude and longitude of the current location
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            float speed = location.getSpeed();

            // Send the location and g-force data to the server
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

            Log.d("GPSLocationSender", timeStamp+"Sending location and g-force data to server: " + latitude + ", " + longitude + ", " + speed + ", " + gforceValue);
            sendLocationData(compNumber, latitude, longitude, speed, gforceValue);
        }
    }

    public void sendLocationData(int id, double latitude, double longitude, float speed, double gForceValues) {
        // Create a JSON object with the location and g-force data
        JSONObject locationData = new JSONObject();
        try {
            locationData.put("id", id);
            locationData.put("latitude", latitude);
            locationData.put("longitude", longitude);
            locationData.put("speed", speed);

            locationData.put("gForce", gforceValue);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // Send an HTTP POST request with the location and g-force data in the request body
        gforceValue = -1;
        RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, this.url, locationData,
                response -> {
                    // Request was successful, do something with the response
                    Log.d("HTTP Response", response.toString());
                },
                error -> {
                    // Request failed, show an error message or take some other action
                    Log.e("HTTP Error", error.toString());
                }
        );
        queue.add(request);
        request.markDelivered();
    }

}

