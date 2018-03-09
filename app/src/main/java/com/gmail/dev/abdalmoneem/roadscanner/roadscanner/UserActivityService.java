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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.gmail.dev.abdalmoneem.roadscanner.roadscanner.Models.Acceleration;
import com.gmail.dev.abdalmoneem.roadscanner.roadscanner.Models.Measurement;
import com.gmail.dev.abdalmoneem.roadscanner.roadscanner.Models.ResultModel;
import com.gmail.dev.abdalmoneem.roadscanner.roadscanner.Models.Trip;
import com.gmail.dev.abdalmoneem.roadscanner.roadscanner.WebApi.ApiUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * Created by Abd on 12/14/2017.
 */

public class UserActivityService extends IntentService implements SensorEventListener {
    protected static final String TAG = "UserActivityServiceClass";
    private static long UPDATE_INTERVAL =  1000;
    private static long FASTEST_INTERVAL = 1000;

    static int lastActivity = DetectedActivity.UNKNOWN;
    static float mLastAx, mLastAy, mLastAz;
//    static ArrayList<Acceleration> acceleartionValues = new ArrayList<Acceleration>() ;
    static double mLastLongitude, mLastLatitude;
    static Trip trip;

    static boolean IsSaving =false;

    //    private static LocationManager locationManager;
    private static FusedLocationProviderClient mFusedLocationClient;
    private static LocationCallback mLocationCallback;
    private static LocationRequest mLocationRequest;


    private static SensorManager sensorManager;
    private static Sensor accelerometer;
    Handler handler;

    public UserActivityService() {
        super(TAG);
        if (trip == null)
            trip = new Trip();
        handler = new Handler(Looper.getMainLooper()); //for toast show


    }


    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {

        super.onCreate();
//        if (locationManager == null)
//            locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        if (mLocationCallback == null)
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    // do work here
                    Location location = locationResult.getLastLocation();
                    if (location != null){
                        mLastLatitude = location.getLatitude();
                        mLastLongitude = location.getLongitude();

                        if (mLastLongitude != 0 && mLastLatitude!=0 && lastActivity == DetectedActivity.IN_VEHICLE) {
                            Measurement measurement = new Measurement(mLastLongitude, mLastLatitude, mLastAx, mLastAy, mLastAz, Calendar.getInstance().getTime());
//                            measurement.setAccelerations((ArrayList<Acceleration>)acceleartionValues.clone());
                            trip.getMeasurements().add(measurement);
//                            acceleartionValues.clear();
                        }

//                        if (trip.getMeasurements().size()>=99)
//                            SaveTrip();
                    }
                    else
                    {
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Location is null",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            };

        if (mFusedLocationClient == null) {
            if (mLocationRequest == null) {
                mLocationRequest = new LocationRequest();
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mLocationRequest.setInterval(UPDATE_INTERVAL);
                mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            }

            // Create LocationSettingsRequest object using location request
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            LocationSettingsRequest locationSettingsRequest = builder.build();

            // Check whether location settings are satisfied
            SettingsClient settingsClient = LocationServices.getSettingsClient(this);
            settingsClient.checkLocationSettings(locationSettingsRequest);

            mFusedLocationClient = getFusedLocationProviderClient(this);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }


    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities(result.getMostProbableActivity());
        }
    }

    private void handleDetectedActivities(DetectedActivity detetedActivity) {

        if (detetedActivity.getType() == DetectedActivity.IN_VEHICLE) //&& maxActivity.getConfidence() >= 75
        {
            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "In Vehicle:",
                            Toast.LENGTH_SHORT).show();
                }
            });
            Log.e("ActivityRecogition", "In Vehicle: " + detetedActivity.getConfidence());

            if (lastActivity != DetectedActivity.IN_VEHICLE && trip.getMeasurements().size()== 0) {
                StartMovementTracking();
                lastActivity = detetedActivity.getType();
            }
        } else if ((detetedActivity.getType() == DetectedActivity.ON_FOOT))  //&& maxActivity.getConfidence() >= 75
        {

            Log.e("ActivityRecogition", "ON FOOT: " + detetedActivity.getConfidence());

            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "on foot",
                            Toast.LENGTH_SHORT).show();
                }
            });


            StopMovementService();

            lastActivity = detetedActivity.getType();

        } else {


            Log.e("ActivityRecogition", "Not In vehicle nor  on foot: " + detetedActivity.getType() + "," + detetedActivity.getConfidence());

            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "3",
                            Toast.LENGTH_SHORT).show();
                }
            });

            if (lastActivity != DetectedActivity.IN_VEHICLE && trip.getMeasurements().size()>0  )
            {
                StopMovementService();
            }

        }
    }

    @SuppressLint("MissingPermission")
    private void StartMovementTracking() {

        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        "Trip started ",
                        Toast.LENGTH_SHORT).show();
            }
        });

        if (sensorManager == null) {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener(this, accelerometer, 2000000);
        }




//        locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);
//        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, this);


    }

    private void StopMovementService() {
        if (mFusedLocationClient != null)
        {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            mFusedLocationClient = null;

        }
        if (sensorManager != null)
        {
            sensorManager.unregisterListener(this);
            sensorManager = null;
        }

//        handler.post(new Runnable() {
//
//            @Override
//            public void run() {
//                Toast.makeText(getApplicationContext(),
//                        "Trip end, measurements count: "+ trip.getMeasurements().size(),
//                        Toast.LENGTH_SHORT).show();
//            }
//        });

        SaveTrip();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            mLastAx = event.values[0];
            mLastAy = event.values[1];
            mLastAz = event.values[2];

           // acceleartionValues.add(new Acceleration(event.values[0],event.values[1],event.values[2]) );

//            @SuppressLint("MissingPermission")  Location lastlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            if (lastlocation == null)
//            {
//                lastlocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//            }

//            if (lastlocation != null) {
//
//                mLastLatitude = lastlocation.getLatitude();
//                mLastLongitude = lastlocation.getLongitude();
//            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void SaveTrip() {
        if (trip.getMeasurements().size() > 0 && !IsSaving) {
            IsSaving =true;

            Call<ResultModel> call = ApiUtils.getAPIService().SaveTrip(trip);
            call.enqueue(new Callback<ResultModel>() {
                @Override
                public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                    final int statusCode = response.code();
                    ResultModel resMod = response.body();

                    if (statusCode == 200)
                        trip.getMeasurements().clear();

                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Save Trip Response code: "+ statusCode,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    IsSaving =false;
                }

                @Override
                public void onFailure(Call<ResultModel> call, Throwable t) {
                    // Log error here since request failed
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Failed to Save Trip : ",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

//                    if (trip.getMeasurements().size()>200)
//                        trip.getMeasurements().clear();

                    Log.e("saveError",t.getMessage()+"\n"+ t.getStackTrace());

                    IsSaving =false;
                }
            });

        }
    }

//    private void SaveTestTrip() {
//        Trip testTrip = new Trip();
//        testTrip.setSerial("test");
//        testTrip.getMeasurements().add(new Measurement(0,0,0,0,0, new Date()));
//
//        Call<ResultModel> call = ApiUtils.getAPIService().SaveTrip(testTrip);
//        call.enqueue(new Callback<ResultModel>() {
//            @Override
//            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
//                final int statusCode = response.code();
//                ResultModel resMod = response.body();
//
//                handler.post(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        Toast.makeText(getApplicationContext(),
//                                "Save Trip Response code: "+ statusCode,
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//
//            @Override
//            public void onFailure(Call<ResultModel> call, Throwable t) {
//                // Log error here since request failed
//                handler.post(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        Toast.makeText(getApplicationContext(),
//                                "Failed to Save Trip : ",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        });
//    }

//    @Override
//    public void onLocationChanged(Location location) {
//        Log.v("location", "IN ON LOCATION CHANGE, lat=" + location.getLatitude() + ", lon=" + location.getLongitude());
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//    }
}
