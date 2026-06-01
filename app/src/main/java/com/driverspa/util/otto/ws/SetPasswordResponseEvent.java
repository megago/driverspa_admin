package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.OkResponseHolder;

public class SetPasswordResponseEvent {

    private OkResponseHolder data;

    public SetPasswordResponseEvent(OkResponseHolder data) {
        this.data = data;
    }

    public OkResponseHolder getData() {
        return data;
    }
}
