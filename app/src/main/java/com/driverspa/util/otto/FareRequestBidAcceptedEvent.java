package com.driverspa.util.otto;

/**
 * Created by Yerzhan Tanatov on 16/01/18.
 */
public class FareRequestBidAcceptedEvent {
    String bookId;

    public FareRequestBidAcceptedEvent(String bookId) {
        this.bookId = bookId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
}
