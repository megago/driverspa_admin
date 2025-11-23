package com.driverspa.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Yerzhan Tanatov on 05/07/16.
 */
public class UserLocation {

    public UserLocation(){}

    public UserLocation(String city, ArrayList<Double> lonLat) {
        this.city = city;
        this.lonLat = lonLat;
    }

    private String city;

    @SerializedName("lonlat")
    private ArrayList<Double> lonLat;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public ArrayList<Double> getLonLat() {
        return lonLat;
    }

    public void setLonLat(ArrayList<Double> lonLat) {
        this.lonLat = lonLat;
    }
}
