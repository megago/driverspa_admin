package com.driverspa.model.api.request;

import com.google.gson.annotations.SerializedName;

/**
 * Body for POST /v1/account/whatsapp_request/. The user isn't logged in yet, so
 * the device FCM token is passed as push_token — it's the only way the backend
 * can push the {type:"wa_verified"} notification once the message arrives.
 */
public class WhatsappRequest {

    @SerializedName("mobile")
    private String mobile;
    @SerializedName("push_token")
    private String pushToken;
    @SerializedName("email")
    private String email;
    @SerializedName("company_name")
    private String companyName;

    public WhatsappRequest(String mobile, String pushToken, String email, String companyName) {
        this.mobile = mobile;
        this.pushToken = pushToken;
        this.email = email;
        this.companyName = companyName;
    }

    public String getMobile() {
        return mobile;
    }

    public String getPushToken() {
        return pushToken;
    }

    public String getEmail() {
        return email;
    }

    public String getCompanyName() {
        return companyName;
    }
}
