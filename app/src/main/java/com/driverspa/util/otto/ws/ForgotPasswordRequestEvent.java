package com.driverspa.util.otto.ws;

import com.driverspa.model.api.request.ForgotPasswordRequest;

public class ForgotPasswordRequestEvent {

    private ForgotPasswordRequest request;

    public ForgotPasswordRequestEvent(ForgotPasswordRequest request) {
        this.request = request;
    }

    public ForgotPasswordRequest getRequest() {
        return request;
    }
}
