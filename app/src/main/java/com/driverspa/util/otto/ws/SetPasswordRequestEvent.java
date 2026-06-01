package com.driverspa.util.otto.ws;

import com.driverspa.model.api.request.SetPasswordRequest;

public class SetPasswordRequestEvent {

    private SetPasswordRequest request;

    public SetPasswordRequestEvent(SetPasswordRequest request) {
        this.request = request;
    }

    public SetPasswordRequest getRequest() {
        return request;
    }
}
