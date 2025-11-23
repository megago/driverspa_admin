package com.driverspa.util.otto.ws;

import com.driverspa.model.api.request.ClientCreateRequest;

/**
 * Created by Yerzhan Tanatov on 14/04/17.
 */
public class ClientEditRequestEvent {
    ClientCreateRequest request;

    public ClientEditRequestEvent(ClientCreateRequest request) {
        this.request = request;
    }

    public ClientCreateRequest getRequest() {
        return request;
    }

    public void setRequest(ClientCreateRequest request) {
        this.request = request;
    }
}
