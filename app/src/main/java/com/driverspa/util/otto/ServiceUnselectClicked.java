package com.driverspa.util.otto;

/**
 * Created by Yerzhan Tanatov on 18/04/17.
 */
public class ServiceUnselectClicked {
    Integer position;

    public ServiceUnselectClicked(Integer position) {
        this.position = position;
    }

    public Integer getPosition() {
        return position;
    }
}
