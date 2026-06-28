package com.driverspa.model.api.request;

import com.google.gson.annotations.SerializedName;

/**
 * Body for POST /v1/account/whatsapp_status/. Carries the short WhatsApp token
 * returned by whatsapp_request (NOT the api_key) — that token is what the
 * backend uses to look up the pending verification.
 */
public class WhatsappStatusRequest {

    @SerializedName("token")
    private String token;

    public WhatsappStatusRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
