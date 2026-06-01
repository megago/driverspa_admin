package com.driverspa.model.api.request;

import com.google.gson.annotations.SerializedName;

public class ForgotPasswordRequest {

    @SerializedName("mobile")
    private String mobile;

    public ForgotPasswordRequest(String mobile) {
        this.mobile = mobile;
    }

    public String getMobile() {
        return mobile;
    }
}
