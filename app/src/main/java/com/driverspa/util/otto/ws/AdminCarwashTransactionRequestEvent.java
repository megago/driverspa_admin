package com.driverspa.util.otto.ws;


public class AdminCarwashTransactionRequestEvent {
     private String washerId;
     private String id;

	 public AdminCarwashTransactionRequestEvent(String washerId, String id){
    	 this.washerId = washerId;
    	 this.id = id;
     }
	
    public String getWasherId() {
		return washerId;
	}

	public void setWasherId(String washerId) {
		this.washerId = washerId;
	}

	public String getId() {
		return id;
	}
}
