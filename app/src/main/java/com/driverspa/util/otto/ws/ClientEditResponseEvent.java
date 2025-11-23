package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.BaseResponseHolder;

/**
 * Created by Yerzhan Tanatov on 14/04/17.
 */
public class ClientEditResponseEvent {
   BaseResponseHolder data;

    public ClientEditResponseEvent(BaseResponseHolder data) {
        this.data = data;
    }

    public BaseResponseHolder getData() {
        return data;
    }
}
