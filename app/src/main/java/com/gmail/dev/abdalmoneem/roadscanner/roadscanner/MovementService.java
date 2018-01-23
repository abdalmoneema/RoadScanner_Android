package com.gmail.dev.abdalmoneem.roadscanner.roadscanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
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
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.gmail.dev.abdalmoneem.roadscanner.roadscanner.Models.Measurement;
import com.gmail.dev.abdalmoneem.roadscanner.roadscanner.Models.Trip;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.gson.Gson;

import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Abd on 12/9/2017.
 */

public class MovementService extends IntentService implements SensorEventListener {

    protected static final String TAG = "MovementServiceClass";
    Location mLastLocation;
    Trip trip;
    private LocationListener listener;
    private LocationManager locationManager;
    private SensorManager sensorManager;
    private Sensor accelerometer;


    public MovementService() {
        super(TAG);

        mLastLocation = new Location(LocationManager.GPS_PROVIDER);
        trip = new Trip();
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mLastLocation.set(location);


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };


    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        //noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, listener);


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, 2000000);

        return result;
    }


    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(listener);
        }

//        if (sensorManager != null) {
//            sensorManager.unregisterListener(this);
//        }
        SaveTrip();
    }


    private void SaveTrip() {
        FileOutputStream outputStream;
        Long tsLong = System.currentTimeMillis() / 1000;
        String filename = tsLong.toString();

        if (trip.getMeasurements().size() > 0) {
            ApiUtils.getAPIService().SaveTrip(trip);
//            Gson gson = new Gson();
//            String json = gson.toJson(trip);
//
//            try {
//                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
//                outputStream.write(json.getBytes());
//                outputStream.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            final float Ax = event.values[0];
            final float Ay = event.values[1];
            final float Az = event.values[2];

            //final float speed = mLastLocation.getSpeed();


            Measurement measurement = new Measurement(mLastLocation.getLongitude(), mLastLocation.getLatitude(), Ax, Ay, Az,  Calendar.getInstance().getTime());

            trip.getMeasurements().add(measurement);

//            Handler handler = new Handler(Looper.getMainLooper());
//            handler.post(new Runnable() {
//
//                @Override
//                public void run() {
//                    Toast.makeText(getApplicationContext(),
//                            "Ax:" + Ax + ",Ay:" + Ay + ", Az:" + Az + ", Speed:" + speed,
//                            Toast.LENGTH_SHORT).show();
//                }
//            });

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
