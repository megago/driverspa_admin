package com.driverspa.util.otto.ws;

public class AdminBoxListRequestEvent {
     private String washerId;
     
	 public AdminBoxListRequestEvent(String washerId){
    	 this.washerId = washerId;
     }
	
    public String getWasherId() {
		return washerId;
	}

	public void setWasherId(String washerId) {
		this.washerId = washerId;
	}

}
