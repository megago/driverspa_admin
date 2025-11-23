package com.driverspa.util.otto;

import java.util.List;

import com.driverspa.model.Washer;

/**
 * Created by Yerzhan Tanatov on 30/06/16.
 */
public class DummyWashersRequestEvent {
    List<Washer> requestList;

    public DummyWashersRequestEvent(List<Washer> requestList) {
        this.requestList = requestList;
    }

    public List<Washer> getRequestList() {
        return requestList;
    }

    public void setRequestList(List<Washer> requestList) {
        this.requestList = requestList;
    }
}
