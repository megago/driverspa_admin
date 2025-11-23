package com.driverspa.util.otto.ws;

/**
 * Created by Yerzhan Tanatov on 14/04/17.
 */
public class ClientRemoveRequestEvent {
    String id;

    public ClientRemoveRequestEvent(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
