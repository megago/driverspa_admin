package com.driverspa.util.otto.ws;

public class AdminWorkshiftsRequestEvent {
     private String washerId;

	 public AdminWorkshiftsRequestEvent(String washerId){
    	 this.washerId = washerId;
     }
	
    public String getWasherId() {
		return washerId;
	}

	public void setWasherId(String washerId) {
		this.washerId = washerId;
	}

}
