package com.gmail.dev.abdalmoneem.roadscanner.roadscanner;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gmail.dev.abdalmoneem.roadscanner.roadscanner.Models.Anomaly;
import com.gmail.dev.abdalmoneem.roadscanner.roadscanner.Models.Measurement;
import com.gmail.dev.abdalmoneem.roadscanner.roadscanner.Models.ResultModel;
import com.gmail.dev.abdalmoneem.roadscanner.roadscanner.WebApi.ApiUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public GoogleApiClient mApiClient;

    Button btnSetBump, btnSetPothole, btnStartMovementService, btnStopMovementService,btnShowMap;
    Handler handler;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private double mLastLongitude, mLastLatitude;

    public MainActivity() {
        handler = new Handler(Looper.getMainLooper()); //for toast show
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();

        if (mLocationCallback == null)
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    // do work here
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        mLastLatitude = location.getLatitude();
                        mLastLongitude = location.getLongitude();
                    } else {
                        mLastLatitude = 0;
                        mLastLongitude = 0;
                    }
                }
            };

        btnSetBump = (Button) findViewById(R.id.btnSetBump);
        btnSetPothole = (Button) findViewById(R.id.btnSetPothole);
        btnStartMovementService = (Button) findViewById(R.id.btnStartMovementService);
        btnStopMovementService = (Button) findViewById(R.id.btnStopMovementService);
        btnShowMap = (Button) findViewById(R.id.btnShowMap);

        btnShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
            }
        });

        btnSetBump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mFusedLocationClient = getFusedLocationProviderClient(getApplicationContext());
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

                if (mLastLongitude != 0 && mLastLatitude!=0) {
                    Anomaly anomaly = new Anomaly(mLastLongitude, mLastLatitude,1);

                    Call<ResultModel> call = ApiUtils.getAPIService().SaveAnomaly(anomaly);
                    call.enqueue(new Callback<ResultModel>() {
                        @Override
                        public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                            final int statusCode = response.code();
                            ResultModel resMod = response.body();

                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),
                                            "Save Anomaly Response code: "+ statusCode,
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
                                            "Failed to Save Anomaly : ",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                            Log.e("saveError",t.getMessage()+"\n"+ t.getStackTrace());

                        }
                    });

                }


            }
        });
        requestPermissions();

//        Intent myIntent = new Intent(getApplicationContext(), UserActivityService.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),  0, myIntent, 0);
//
//        AlarmManager alarmManager = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
//        calendar.add(Calendar.SECOND, 2); // first time
//        long frequency= 2 * 1000; // in ms
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), frequency, pendingIntent);

    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT > 22 ) {
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100)
        {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent( this, UserActivityService.class );
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, 5000, pendingIntent );
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
