package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

public class WhatsappRequestResponseHolder extends BaseResponseHolder {

    @SerializedName("data")
    private WhatsappRequestData response;

    public WhatsappRequestData getResponse() {
        return response;
    }
}
