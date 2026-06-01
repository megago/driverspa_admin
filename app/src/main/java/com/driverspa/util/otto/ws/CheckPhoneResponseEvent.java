package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.CheckPhoneResponseHolder;

public class CheckPhoneResponseEvent {

    private CheckPhoneResponseHolder data;

    public CheckPhoneResponseEvent(CheckPhoneResponseHolder data) {
        this.data = data;
    }

    public CheckPhoneResponseHolder getData() {
        return data;
    }
}
