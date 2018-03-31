package com.gmail.dev.abdalmoneem.roadscanner.roadscanner.WebApi;

/**
 * Created by Abd on 1/6/2018.
 */

public class ApiUtils {
    private ApiUtils() {}

    public static final String BASE_URL = "http://www.roadscanner.somee.com/";

    public static RoadScannerAPIService getAPIService() {

        return ApiClient.getClient(BASE_URL).create(RoadScannerAPIService.class);
    }
}
