package com.driverspa.util.otto.ws;

import com.driverspa.model.api.request.WhatsappStatusRequest;

public class WhatsappStatusRequestEvent {

    private final WhatsappStatusRequest request;

    public WhatsappStatusRequestEvent(WhatsappStatusRequest request) {
        this.request = request;
    }

    public WhatsappStatusRequest getRequest() {
        return request;
    }
}
