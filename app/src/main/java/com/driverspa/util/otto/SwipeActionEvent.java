package com.driverspa.util.otto;

/**
 * Created by Yerzhan Tanatov on 23/12/16.
 */
public class SwipeActionEvent {
    boolean swipeRight;

    public SwipeActionEvent(boolean swipeRight) {
        this.swipeRight = swipeRight;
    }

    public boolean isSwipeRight() {
        return swipeRight;
    }

    public void setSwipeRight(boolean swipeRight) {
        this.swipeRight = swipeRight;
    }
}
