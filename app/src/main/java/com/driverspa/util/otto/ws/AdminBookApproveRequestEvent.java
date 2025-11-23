package com.driverspa.util.otto.ws;

public class AdminBookApproveRequestEvent {
     private String bookId;
     
	 public String getBookId() {
		return bookId;
	}

	public void setBookId(String bookId) {
		this.bookId = bookId;
	}

	public AdminBookApproveRequestEvent(String bookId){
    	 this.bookId = bookId;
     }
	
}
