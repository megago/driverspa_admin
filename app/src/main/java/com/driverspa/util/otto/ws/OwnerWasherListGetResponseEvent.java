package com.driverspa.util.otto.ws;

import java.util.List;

import com.driverspa.model.WasherPublic;

/**
 * Created by Yerzhan Tanatov on 20/12/15.
 */
public class OwnerWasherListGetResponseEvent {
    List<WasherPublic> washerList;

    public OwnerWasherListGetResponseEvent(List<WasherPublic> washerList) {
        this.washerList = washerList;
    }

    public List<WasherPublic> getWasherList() {
        return washerList;
    }

    public void setWasherList(List<WasherPublic> washer) {
        this.washerList = washerList;
    }
}
