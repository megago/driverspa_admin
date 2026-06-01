package com.driverspa.util.otto.ws;

import com.driverspa.model.api.request.CheckPhoneRequest;

public class CheckPhoneRequestEvent {

    private CheckPhoneRequest request;

    public CheckPhoneRequestEvent(CheckPhoneRequest request) {
        this.request = request;
    }

    public CheckPhoneRequest getRequest() {
        return request;
    }
}
