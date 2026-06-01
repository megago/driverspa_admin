package com.driverspa.model.api.request;

import com.google.gson.annotations.SerializedName;

public class CheckPhoneRequest {

    @SerializedName("mobile")
    private String mobile;

    public CheckPhoneRequest(String mobile) {
        this.mobile = mobile;
    }

    public String getMobile() {
        return mobile;
    }
}
