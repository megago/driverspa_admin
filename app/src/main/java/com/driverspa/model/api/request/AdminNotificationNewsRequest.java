package com.driverspa.model.api.request;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Yerzhan Tanatov on 06/02/17.
 */
public class AdminNotificationNewsRequest {

    String carwash;
    String mobile;
    @SerializedName("news_target")
    String newsTarget;
    @SerializedName("news_type")
    String newsType;
    String status;
    @SerializedName("news_text")
    String text;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCarwash() {
        return carwash;
    }

    public void setCarwash(String carwash) {
        this.carwash = carwash;
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
}
