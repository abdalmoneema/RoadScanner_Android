package com.gmail.dev.abdalmoneem.roadscanner.roadscanner.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Abd on 3/3/2018.
 */

public class Anomaly {
    @SerializedName("Longitude")
    double Longitude ;

    @SerializedName("Latitude")
    double Latitude ;

    @SerializedName("Type")
    public int Type;

    public Anomaly(double longitude, double latitude, int type) {
        Longitude = longitude;
        Latitude = latitude;
        Type = type;
    }
}
