package com.driverspa.util.otto;

import com.driverspa.model.PushData;

/**
 * Created by Yerzhan Tanatov on 06/07/16.
 */
public class NewInformationPushRequestEvent {
    PushData data;

    public NewInformationPushRequestEvent(PushData data) {
        this.data = data;
    }

    public PushData getData() {
        return data;
    }

    public void setData(PushData data) {
        this.data = data;
    }
}
