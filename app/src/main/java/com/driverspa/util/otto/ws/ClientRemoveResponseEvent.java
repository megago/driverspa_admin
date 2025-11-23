package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.BaseResponseHolder;

/**
 * Created by Yerzhan Tanatov on 14/04/17.
 */
public class ClientRemoveResponseEvent {
   BaseResponseHolder data;

    public ClientRemoveResponseEvent(BaseResponseHolder data) {
        this.data = data;
    }

    public BaseResponseHolder getData() {
        return data;
    }
}
