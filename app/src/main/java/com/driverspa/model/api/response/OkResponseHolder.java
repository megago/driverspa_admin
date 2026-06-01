package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

public class OkResponseHolder extends BaseResponseHolder {

    @SerializedName("data")
    private OkData response;

    public OkData getResponse() {
        return response;
    }
}
