package com.gmail.dev.abdalmoneem.roadscanner.roadscanner;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
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
import android.util.Log;
import android.widget.Toast;

import com.gmail.dev.abdalmoneem.roadscanner.roadscanner.Models.Measurement;
import com.gmail.dev.abdalmoneem.roadscanner.roadscanner.Models.ResultModel;
import com.gmail.dev.abdalmoneem.roadscanner.roadscanner.Models.Trip;
import com.gmail.dev.abdalmoneem.roadscanner.roadscanner.WebApi.ApiUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Abd on 12/14/2017.
 */

public class UserActivityService extends IntentService implements SensorEventListener {
    protected static final String TAG = "UserActivityServiceClass";

    static int lastActivity = DetectedActivity.UNKNOWN;
    static float mLastAx,mLastAy,mLastAz;
    static double mLastLongitude,mLastLatitude;
    static Trip trip;

    private static LocationManager locationManager;
    private static SensorManager sensorManager;
    private static Sensor accelerometer;
    Handler handler ;

    public UserActivityService() {
        super(TAG);
        if (trip == null)
            trip = new Trip();
        handler = new Handler(Looper.getMainLooper()); //for toast show


    }



    @Override
    public void onCreate() {

        super.onCreate();
        if (locationManager == null)
            locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
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

            if (lastActivity != DetectedActivity.IN_VEHICLE) {
                StartMovementTracking();
                lastActivity = detetedActivity.getType();
            }

            if (mLastLongitude != 0 && mLastLatitude!=0) {
                Measurement measurement = new Measurement(mLastLongitude, mLastLatitude, mLastAx, mLastAy, mLastAz, Calendar.getInstance().getTime());
                trip.getMeasurements().add(measurement);
            }

            if (trip.getMeasurements().size()>=99)
                SaveTrip();

        } else if ((detetedActivity.getType() == DetectedActivity.ON_FOOT))  //&& maxActivity.getConfidence() >= 75
        {

            Log.e("ActivityRecogition", "ON FOOT: " + detetedActivity.getConfidence());

            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "Activity: on foot",
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
                            "Not In vehicle nor  on foot: ",
                            Toast.LENGTH_SHORT).show();
                }
            });



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
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, 2000000);
        }




//        locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);
//        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, this);


    }

    private void StopMovementService() {
//        if (locationManager != null)
//        {
//            locationManager.removeUpdates(this);
//            locationManager = null;
//
//        }
        if (sensorManager != null)
        {
            sensorManager.unregisterListener(this);
            sensorManager = null;
        }

        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        "Trip end, measurements count: "+ trip.getMeasurements().size(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        SaveTrip();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mLastAx = event.values[0];
            mLastAy = event.values[1];
            mLastAz = event.values[2];

            @SuppressLint("MissingPermission")  Location lastlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastlocation == null)
            {
                lastlocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (lastlocation != null) {

                mLastLatitude = lastlocation.getLatitude();
                mLastLongitude = lastlocation.getLongitude();
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void SaveTrip() {
        if (trip.getMeasurements().size() > 0) {
            Call<ResultModel> call = ApiUtils.getAPIService().SaveTrip(trip);
            call.enqueue(new Callback<ResultModel>() {
                @Override
                public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                    final int statusCode = response.code();
                    ResultModel resMod = response.body();

                    trip.getMeasurements().clear();

                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Save Trip Response code: "+ statusCode,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
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

                    if (trip.getMeasurements().size()>200)
                        trip.getMeasurements().clear();

                    Log.e("saveError",t.getMessage()+"\n"+ t.getStackTrace());
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
