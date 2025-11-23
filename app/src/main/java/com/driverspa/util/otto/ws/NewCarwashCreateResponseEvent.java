package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.WasherResponseHolder;

/**
 * Created by Yerzhan Tanatov on 11/04/17.
 */
public class NewCarwashCreateResponseEvent {
    WasherResponseHolder data;

    public NewCarwashCreateResponseEvent(WasherResponseHolder data) {
        this.data = data;
    }

    public WasherResponseHolder getData() {
        return data;
    }

    public void setData(WasherResponseHolder data) {
        this.data = data;
    }
}
