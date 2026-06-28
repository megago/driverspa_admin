package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.WhatsappRequestResponseHolder;

public class WhatsappRequestResponseEvent {

    private final WhatsappRequestResponseHolder data;

    public WhatsappRequestResponseEvent(WhatsappRequestResponseHolder data) {
        this.data = data;
    }

    public WhatsappRequestResponseHolder getData() {
        return data;
    }
}
