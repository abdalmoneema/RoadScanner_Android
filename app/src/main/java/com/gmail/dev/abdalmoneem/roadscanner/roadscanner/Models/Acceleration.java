package com.gmail.dev.abdalmoneem.roadscanner.roadscanner.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Abd on 2/19/2018.
 */


    public class Acceleration {

        @SerializedName("AccelerationX")
        @Expose
        private float accelerationX;
        @SerializedName("AccelerationY")
        @Expose
        private float accelerationY;
        @SerializedName("AccelerationZ")
        @Expose
        private float accelerationZ;

        public float getAccelerationX() {
            return accelerationX;
        }

        public void setAccelerationX(float accelerationX) {
            this.accelerationX = accelerationX;
        }

        public float getAccelerationY() {
            return accelerationY;
        }

        public void setAccelerationY(float accelerationY) {
            this.accelerationY = accelerationY;
        }

        public float getAccelerationZ() {
            return accelerationZ;
        }

        public void setAccelerationZ(float accelerationZ) {
            this.accelerationZ = accelerationZ;
        }

    public Acceleration(float accelerationX, float accelerationY, float accelerationZ) {
        this.accelerationX = accelerationX;
        this.accelerationY = accelerationY;
        this.accelerationZ = accelerationZ;
    }
}
