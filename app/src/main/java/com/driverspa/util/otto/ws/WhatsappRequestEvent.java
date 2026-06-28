package com.driverspa.util.otto.ws;

import com.driverspa.model.api.request.WhatsappRequest;

public class WhatsappRequestEvent {

    private final WhatsappRequest request;

    public WhatsappRequestEvent(WhatsappRequest request) {
        this.request = request;
    }

    public WhatsappRequest getRequest() {
        return request;
    }
}
