package edu.softserve.administrator.exampleapplication;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 9/26/2015.
 */
public class WeatherData {

    private static final WeatherData sInstance = new WeatherData();

    public String description;
    public String temperature;
    public String windSpeed;
    public String humidity;
    public String pressure;
    public Bitmap icon;

    public static WeatherData getInstance() {
        return sInstance;
    }

    private WeatherData() {
    }
}

