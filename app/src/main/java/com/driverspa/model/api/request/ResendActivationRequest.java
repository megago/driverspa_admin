package com.driverspa.model.api.request;

import com.google.gson.annotations.SerializedName;

public class ResendActivationRequest {

    @SerializedName("mobile")
    private String mobile;
    // OTP delivery channel: "email" (default), "sms" or "whatsapp".
    @SerializedName("channel")
    private String channel = "email";

    public ResendActivationRequest(String mobile) {
        this.mobile = mobile;
    }

    public ResendActivationRequest(String mobile, String channel) {
        this.mobile = mobile;
        this.channel = channel;
    }

    public String getMobile() {
        return mobile;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
