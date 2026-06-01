package com.driverspa.model.api.request;

import com.google.gson.annotations.SerializedName;

public class SetPasswordRequest {

    @SerializedName("password")
    private String password;
    @SerializedName("password_confirm")
    private String passwordConfirm;

    public SetPasswordRequest(String password, String passwordConfirm) {
        this.password = password;
        this.passwordConfirm = passwordConfirm;
    }

    public String getPassword() {
        return password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }
}
