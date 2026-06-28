package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

import com.driverspa.model.User;

/**
 * "data" of POST /v1/account/whatsapp_status/.
 *
 * While the verification is pending the backend returns {verified:false}; once
 * the WhatsApp message has been matched it returns the full account, including
 * password_required and token (= the api_key, same shape as the activate
 * response). Detect "done" via {@link User#getToken()} / {@link User#isVerified()}.
 */
public class WhatsappStatusResponseHolder extends BaseResponseHolder {

    @SerializedName("data")
    private User response;

    public User getResponse() {
        return response;
    }
}
