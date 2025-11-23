package com.driverspa.util.otto.ws;

public class AdminBookNoClientRequestEvent {
     private String bookId;
     
	 public String getBookId() {
		return bookId;
	}

	public void setBookId(String bookId) {
		this.bookId = bookId;
	}

	public AdminBookNoClientRequestEvent(String bookId){
    	 this.bookId = bookId;
     }
	
}
