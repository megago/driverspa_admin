package com.driverspa.util.otto.ws;

public class AdminWasherInfoRequestEvent {
     private String washerId;
     
	 public AdminWasherInfoRequestEvent(String washerId){
    	 this.washerId = washerId;
     }
	
    public String getWasherId() {
		return washerId;
	}

	public void setWasherId(String washerId) {
		this.washerId = washerId;
	}

}
