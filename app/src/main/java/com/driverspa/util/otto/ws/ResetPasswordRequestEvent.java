package com.driverspa.util.otto.ws;

import com.driverspa.model.api.request.ResetPasswordRequest;

public class ResetPasswordRequestEvent {

    private ResetPasswordRequest request;

    public ResetPasswordRequestEvent(ResetPasswordRequest request) {
        this.request = request;
    }

    public ResetPasswordRequest getRequest() {
        return request;
    }
}
