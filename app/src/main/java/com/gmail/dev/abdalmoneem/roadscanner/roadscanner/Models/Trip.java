package com.gmail.dev.abdalmoneem.roadscanner.roadscanner.Models;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Abd on 1/6/2018.
 */

public class Trip {
    @SerializedName("DeviceID")
    String deviceID ;

    @SerializedName("measurements")
    ArrayList<Measurement> measurements;

    public Trip() {
        deviceID = android.os.Build.SERIAL;
        measurements = new ArrayList<Measurement>() ;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void setSerial(String serial)
    {
        this.deviceID = serial;
    }
}
