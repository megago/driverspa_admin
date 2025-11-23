package com.driverspa.util.otto;

import com.driverspa.model.BookInfo;

/**
 * Created by Yerzhan Tanatov on 01/12/16.
 */
public class BookOptionClickEvent {
    BookInfo book;

    public BookOptionClickEvent(BookInfo book) {
        this.book = book;
    }

    public BookInfo getBook() {
        return book;
    }

    public void setBook(BookInfo book) {
        this.book = book;
    }
}
