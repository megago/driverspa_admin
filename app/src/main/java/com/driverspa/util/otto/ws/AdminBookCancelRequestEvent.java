package com.driverspa.util.otto.ws;

public class AdminBookCancelRequestEvent {
     private String bookId;
     
	 public String getBookId() {
		return bookId;
	}

	public void setBookId(String bookId) {
		this.bookId = bookId;
	}

	public AdminBookCancelRequestEvent(String bookId){
    	 this.bookId = bookId;
     }
	
}
