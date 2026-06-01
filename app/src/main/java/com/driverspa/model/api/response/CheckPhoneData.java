package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

public class CheckPhoneData {

    @SerializedName("exists")
    private boolean exists;
    @SerializedName("has_password")
    private boolean hasPassword;
    @SerializedName("is_active")
    private boolean isActive;

    public boolean isExists() {
        return exists;
    }

    public boolean isHasPassword() {
        return hasPassword;
    }

    public boolean isActive() {
        return isActive;
    }
}
