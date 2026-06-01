package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

public class CheckPhoneResponseHolder extends BaseResponseHolder {

    @SerializedName("data")
    private CheckPhoneData response;

    public CheckPhoneData getResponse() {
        return response;
    }
}
