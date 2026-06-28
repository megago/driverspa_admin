package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

/**
 * "data" payload of POST /v1/account/whatsapp_request/.
 *
 * NB: {@link #token} here is the SHORT WhatsApp token — it is sent in the
 * WhatsApp message and used to poll whatsapp_status. It is NOT the api_key.
 */
public class WhatsappRequestData {

    @SerializedName("token")
    private String token;
    @SerializedName("whatsapp_number")
    private String whatsappNumber;
    @SerializedName("wa_link")
    private String waLink;

    public String getToken() {
        return token;
    }

    public String getWhatsappNumber() {
        return whatsappNumber;
    }

    public String getWaLink() {
        return waLink;
    }
}
