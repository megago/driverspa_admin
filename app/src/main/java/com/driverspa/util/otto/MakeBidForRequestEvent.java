package com.driverspa.util.otto;

import com.driverspa.model.FareRequest;

/**
 * Created by Yerzhan Tanatov on 16/01/18.
 */
public class MakeBidForRequestEvent {
    FareRequest request;

    public MakeBidForRequestEvent(FareRequest request) {
        this.request = request;
    }

    public FareRequest getRequest() {
        return request;
    }
}
