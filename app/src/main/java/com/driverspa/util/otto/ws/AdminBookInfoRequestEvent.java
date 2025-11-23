package com.driverspa.util.otto.ws;

public class AdminBookInfoRequestEvent {
     private String bookId;
     
	 public String getBookId() {
		return bookId;
	}

	public void setBookId(String bookId) {
		this.bookId = bookId;
	}

	public AdminBookInfoRequestEvent(String bookId){
    	 this.bookId = bookId;
     }
	
}
