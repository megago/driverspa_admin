package com.driverspa.model.api.request;

import com.google.gson.annotations.SerializedName;

public class ForgotPasswordRequest {

    @SerializedName("mobile")
    private String mobile;
    // OTP delivery channel: "whatsapp" (default) or "sms".
    @SerializedName("channel")
    private String channel = "whatsapp";

    public ForgotPasswordRequest(String mobile) {
        this.mobile = mobile;
    }

    public ForgotPasswordRequest(String mobile, String channel) {
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
