package com.driverspa.model.api.request;

import com.google.gson.annotations.SerializedName;

public class LoginPasswordRequest {

    @SerializedName("mobile")
    private String mobile;
    @SerializedName("password")
    private String password;

    public LoginPasswordRequest(String mobile, String password) {
        this.mobile = mobile;
        this.password = password;
    }

    public String getMobile() {
        return mobile;
    }

    public String getPassword() {
        return password;
    }
}
