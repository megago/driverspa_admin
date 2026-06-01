package com.driverspa.util.otto.ws;

import com.driverspa.model.api.request.LoginPasswordRequest;

public class LoginPasswordRequestEvent {

    private LoginPasswordRequest request;

    public LoginPasswordRequestEvent(LoginPasswordRequest request) {
        this.request = request;
    }

    public LoginPasswordRequest getRequest() {
        return request;
    }
}
