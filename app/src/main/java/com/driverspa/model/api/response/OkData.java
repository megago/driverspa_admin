package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

public class OkData {

    @SerializedName("ok")
    private boolean ok;

    public boolean isOk() {
        return ok;
    }
}
