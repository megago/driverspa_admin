package com.driverspa.util.otto.ws;

public class AdminGroupMenuListRequestEvent {
     private String washerId;

	 public AdminGroupMenuListRequestEvent(String washerId){
    	 this.washerId = washerId;
     }
	
    public String getWasherId() {
		return washerId;
	}

	public void setWasherId(String washerId) {
		this.washerId = washerId;
	}

}
