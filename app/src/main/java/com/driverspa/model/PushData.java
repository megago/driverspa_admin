package com.driverspa.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Yerzhan Tanatov on 06/07/16.
 */
public class PushData {

//    {
//        "carwash_id": "57749b42da85ee251f81e845",
//            "mark": 4,
//            "object_id": "577c9667da85eed26e4a20a9",
//            "text": "abc",
//            "type": "review_created"
//    }
//
//    {
//        "object_id": "577c9671da85eed26e4a20ab",
//            "text": null,
//            "type": "booking_created"
//    }

    @SerializedName("carwash_id")
    String carwashId;
    Integer mark;
    @SerializedName("object_id")
    String objectId;
    @SerializedName("request_object")
    FareRequest fareRequest;
    String text;
    String type;

    public PushData(String objectId) {
        this.objectId = objectId;
    }

    public String getCarwashId() {
        return carwashId;
    }

    public void setCarwashId(String carwashId) {
        this.carwashId = carwashId;
    }

    public Integer getMark() {
        return mark;
    }

    public void setMark(Integer mark) {
        this.mark = mark;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FareRequest getFareRequest() {
        return fareRequest;
    }

    public void setFareRequest(FareRequest fareRequest) {
        this.fareRequest = fareRequest;
    }

    public String serialize() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static PushData deserialize(String serializedData) {
        Gson gson = new Gson();
        return gson.fromJson(serializedData, PushData.class);
    }
}
