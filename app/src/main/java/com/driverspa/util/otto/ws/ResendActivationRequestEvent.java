package com.driverspa.util.otto.ws;

import com.driverspa.model.api.request.ResendActivationRequest;

public class ResendActivationRequestEvent {

    private ResendActivationRequest request;

    public ResendActivationRequestEvent(ResendActivationRequest request) {
        this.request = request;
    }

    public ResendActivationRequest getRequest() {
        return request;
    }
}
