package com.driverspa.model;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;

/**
 * Created by Yerzhan Tanatov on 06/02/17.
 */
public class AdminNotificationNews {

    String carwash;
    String id;
    @SerializedName("news_target")
    String newsTarget;
    @SerializedName("news_type")
    String newsType;
    String status;
    @SerializedName("news_text")
    String text;
    @SerializedName("ts")
    Instant TS;

    public String getCarwash() {
        return carwash;
    }

    public void setCarwash(String carwash) {
        this.carwash = carwash;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNewsTarget() {
        return newsTarget;
    }

    public void setNewsTarget(String newsTarget) {
        this.newsTarget = newsTarget;
    }

    public String getNewsType() {
        return newsType;
    }

    public void setNewsType(String newsType) {
        this.newsType = newsType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Instant getTS() {
        return TS;
    }

    public void setTS(Instant TS) {
        this.TS = TS;
    }
}
