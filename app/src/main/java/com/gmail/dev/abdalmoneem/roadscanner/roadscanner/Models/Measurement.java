package com.gmail.dev.abdalmoneem.roadscanner.roadscanner.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * Created by Abd on 1/6/2018.
 */

public class Measurement {
    @SerializedName("Longitude")
    double Longitude;
    @SerializedName("Latitude")
    double Latitude;
    @SerializedName("AccelerationX")
    float AccelerationX;
    @SerializedName("AccelerationY")
    float AccelerationY;
    @SerializedName("AccelerationZ")
    float AccelerationZ;
    @SerializedName("MeasurementTime")
    Date MeasurementTime;
//    @SerializedName("Accelerations")
//    @Expose
//    private List<Acceleration> accelerations = null;

//    public List<Acceleration> getAccelerations() {
//        return accelerations;
//    }
//
//    public void setAccelerations(List<Acceleration> accelerations) {
//        this.accelerations = accelerations;
//    }

    public Measurement(double longitude, double latitude, float accelerationX, float accelerationY, float accelerationZ, Date measurementTime) {
        Longitude = longitude;
        Latitude = latitude;
        AccelerationX = accelerationX;
        AccelerationY = accelerationY;
        AccelerationZ = accelerationZ;
        MeasurementTime = measurementTime;
    }
}
