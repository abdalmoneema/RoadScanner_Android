package com.gmail.dev.abdalmoneem.roadscanner.roadscanner;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public GoogleApiClient mApiClient;

    Button btnSetBump ,btnStartMovementService,btnStopMovementService ;
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

        btnSetBump = (Button)findViewById(R.id.btnSetBump);
        btnStartMovementService = (Button)findViewById(R.id.btnStartMovementService);
        btnStopMovementService = (Button)findViewById(R.id.btnStopMovementService);

        requestPermissions();

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
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
            {
                enableButtons();
                StartUserActivityService();
            }
            else
            {
                requestPermissions();
            }
        }
    }

    private void enableButtons() {
        btnSetBump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnStartMovementService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartUserActivityService();
            }
        });

        btnStopMovementService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StopUserActivityService();
            }
        });
    }



    private void StartUserActivityService()
    {
        Intent intent = new Intent(getApplicationContext(),UserActivityService.class);
        startService(intent);
    }

    private void StopUserActivityService() {
        Intent intent = new Intent(getApplicationContext(),UserActivityService.class);
        stopService(intent);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent( this, UserActivityService.class );
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, 3000, pendingIntent );
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
