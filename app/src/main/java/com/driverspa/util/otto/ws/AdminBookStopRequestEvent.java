package com.driverspa.util.otto.ws;

public class AdminBookStopRequestEvent {
     private String bookId;
     
	 public String getBookId() {
		return bookId;
	}

	public void setBookId(String bookId) {
		this.bookId = bookId;
	}

	public AdminBookStopRequestEvent(String bookId){
    	 this.bookId = bookId;
     }
	
}
