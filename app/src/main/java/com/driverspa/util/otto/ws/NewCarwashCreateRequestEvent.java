package com.driverspa.util.otto.ws;

import com.driverspa.model.api.request.InitialWasherCreateRequest;

/**
 * Created by Yerzhan Tanatov on 11/04/17.
 */
public class NewCarwashCreateRequestEvent {
    InitialWasherCreateRequest request;

    public NewCarwashCreateRequestEvent(InitialWasherCreateRequest request) {
        this.request = request;
    }

    public InitialWasherCreateRequest getRequest() {
        return request;
    }

    public void setRequest(InitialWasherCreateRequest request) {
        this.request = request;
    }
}
