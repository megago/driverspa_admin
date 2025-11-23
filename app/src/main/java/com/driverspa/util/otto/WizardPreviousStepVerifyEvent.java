package com.driverspa.util.otto;

/**
 * Created by Yerzhan Tanatov on 12/04/17.
 */
public class WizardPreviousStepVerifyEvent {
    int position;

    public WizardPreviousStepVerifyEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
