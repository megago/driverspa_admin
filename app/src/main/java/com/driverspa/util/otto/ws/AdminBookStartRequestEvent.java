package com.driverspa.util.otto.ws;

public class AdminBookStartRequestEvent {
     private String bookId;
     
	 public String getBookId() {
		return bookId;
	}

	public void setBookId(String bookId) {
		this.bookId = bookId;
	}

	public AdminBookStartRequestEvent(String bookId){
    	 this.bookId = bookId;
     }
	
}
