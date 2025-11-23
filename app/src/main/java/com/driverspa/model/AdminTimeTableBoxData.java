package com.driverspa.model;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Yerzhan Tanatov on 24/12/16.
 */
public class AdminTimeTableBoxData {
    HashMap<Integer,List<BookInfo>> bookingGridObjects;
    HashMap<String,BookInfo> bookingObjects;

    public AdminTimeTableBoxData(HashMap<Integer, List<BookInfo>> bookingGridObjects, HashMap<String, BookInfo> bookingObjects) {
        this.bookingGridObjects = bookingGridObjects;
        this.bookingObjects = bookingObjects;
    }

    public HashMap<Integer, List<BookInfo>> getBookingGridObjects() {
        return bookingGridObjects;
    }

    public void setBookingGridObjects(HashMap<Integer, List<BookInfo>> bookingGridObjects) {
        this.bookingGridObjects = bookingGridObjects;
    }

    public HashMap<String, BookInfo> getBookingObjects() {
        return bookingObjects;
    }

    public void setBookingObjects(HashMap<String, BookInfo> bookingObjects) {
        this.bookingObjects = bookingObjects;
    }

    // GSON
    public String serialize() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static AdminTimeTableBoxData deserialize(String serializedData) {
        Gson gson = new Gson();
        return gson.fromJson(serializedData, AdminTimeTableBoxData.class);
    }

}
