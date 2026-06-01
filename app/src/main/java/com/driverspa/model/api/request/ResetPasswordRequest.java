package com.driverspa.model.api.request;

import com.google.gson.annotations.SerializedName;

public class ResetPasswordRequest {

    @SerializedName("mobile")
    private String mobile;
    @SerializedName("activation_code")
    private String activationCode;
    @SerializedName("password")
    private String password;
    @SerializedName("password_confirm")
    private String passwordConfirm;

    public ResetPasswordRequest(String mobile, String activationCode, String password, String passwordConfirm) {
        this.mobile = mobile;
        this.activationCode = activationCode;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
    }

    public String getMobile() {
        return mobile;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public String getPassword() {
        return password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }
}
