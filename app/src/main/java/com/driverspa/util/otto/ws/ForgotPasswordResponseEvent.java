package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.OkResponseHolder;

public class ForgotPasswordResponseEvent {

    private OkResponseHolder data;

    public ForgotPasswordResponseEvent(OkResponseHolder data) {
        this.data = data;
    }

    public OkResponseHolder getData() {
        return data;
    }
}
