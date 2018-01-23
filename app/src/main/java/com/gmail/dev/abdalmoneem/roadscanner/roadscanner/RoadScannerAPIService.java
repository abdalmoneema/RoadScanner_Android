package com.gmail.dev.abdalmoneem.roadscanner.roadscanner;

import com.gmail.dev.abdalmoneem.roadscanner.roadscanner.Models.ResultModel;
import com.gmail.dev.abdalmoneem.roadscanner.roadscanner.Models.Trip;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Abd on 1/6/2018.
 */

public interface RoadScannerAPIService {
    @POST("api/CreateTrip/")
    Call<ResultModel> SaveTrip(@Body Trip trip);
}
