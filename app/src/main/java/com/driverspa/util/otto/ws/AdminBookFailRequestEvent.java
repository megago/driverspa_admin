package com.driverspa.util.otto.ws;

public class AdminBookFailRequestEvent {
     private String bookId;
     
	 public String getBookId() {
		return bookId;
	}

	public void setBookId(String bookId) {
		this.bookId = bookId;
	}

	public AdminBookFailRequestEvent(String bookId){
    	 this.bookId = bookId;
     }
	
}
