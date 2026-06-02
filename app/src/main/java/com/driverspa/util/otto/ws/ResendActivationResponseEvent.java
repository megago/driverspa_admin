package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.OkResponseHolder;

public class ResendActivationResponseEvent {

    private OkResponseHolder data;

    public ResendActivationResponseEvent(OkResponseHolder data) {
        this.data = data;
    }

    public OkResponseHolder getData() {
        return data;
    }
}
