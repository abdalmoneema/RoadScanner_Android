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
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.gmail.dev.abdalmoneem.roadscanner.roadscanner.Models.Measurement;
import com.gmail.dev.abdalmoneem.roadscanner.roadscanner.Models.ResultModel;
import com.gmail.dev.abdalmoneem.roadscanner.roadscanner.Models.Trip;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Abd on 12/14/2017.
 */

public class UserActivityService extends IntentService implements SensorEventListener {
    protected static final String TAG = "UserActivityServiceClass";
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    DetectedActivity lastActivity;
    //Intent movementServiceIntent;

    float mLastAx;
    float mLastAy;
    float mLastAz;

    Trip trip;
    private LocationListener listener;
    private LocationManager locationManager;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    Handler handler ;

    public UserActivityService() {

        super(TAG);
        trip = new Trip();
        handler = new Handler(Looper.getMainLooper()); //for toast show

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Measurement measurement = new Measurement(location.getLongitude(), location.getLatitude(), mLastAx, mLastAy, mLastAz,  Calendar.getInstance().getTime());
                trip.getMeasurements().add(measurement);
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

    @Override
    public void onCreate() {
        super.onCreate();
//        movementServiceIntent = new Intent(getBaseContext(), MovementService.class);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities(result.getProbableActivities());
        }
    }

    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {

        DetectedActivity maxActivity = Collections.max(probableActivities, new Comparator<DetectedActivity>() {

            public int compare(DetectedActivity o1, DetectedActivity o2) {
                return o1.getConfidence() - o2.getConfidence();
            }
        });


        if (maxActivity.getType() == DetectedActivity.IN_VEHICLE) //&& maxActivity.getConfidence() >= 75
        {
            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "In Vehicle:",
                            Toast.LENGTH_SHORT).show();
                }
            });
            Log.e("ActivityRecogition", "In Vehicle: " + maxActivity.getConfidence());

            if (lastActivity == null || lastActivity.getType() != DetectedActivity.IN_VEHICLE) {
                StartMovementTracking();
                lastActivity = maxActivity;
            }
        } else if ((maxActivity.getType() == DetectedActivity.ON_FOOT))  //&& maxActivity.getConfidence() >= 75
        {
            StopMovementService();

            lastActivity = maxActivity;
            Log.e("ActivityRecogition", "ON FOOT: " + maxActivity.getConfidence());

            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "Activity: on foot",
                            Toast.LENGTH_SHORT).show();
                }
            });


        } else {


            Log.e("ActivityRecogition", "Not In vehicle nor  on foot: " + maxActivity.getType() + "," + maxActivity.getConfidence());

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

        trip.getMeasurements().clear();

        if (sensorManager == null) {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        sensorManager.registerListener(this, accelerometer, 2000000);

        if (locationManager == null)
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, listener);

    }

    private void StopMovementService() {
        if (locationManager != null)
        {
            locationManager.removeUpdates(listener);
        }
        if (sensorManager != null)
        {
            sensorManager.unregisterListener(this);
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mLastAx = event.values[0];
            mLastAy = event.values[1];
            mLastAz = event.values[2];




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

    private void SaveTrip() {
        if (trip.getMeasurements().size() > 0) {
             ApiUtils.getAPIService().SaveTrip(trip);
        }
    }

    private void SaveTestTrip() {
        Trip testTrip = new Trip();
        testTrip.setSerial("test");
        testTrip.getMeasurements().add(new Measurement(0,0,0,0,0, new Date()));

        Call<ResultModel> call = ApiUtils.getAPIService().SaveTrip(testTrip);
        call.enqueue(new Callback<ResultModel>() {
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                int statusCode = response.code();
                ResultModel resMod = response.body();
            }

            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                // Log error here since request failed
            }
        });
    }
}
