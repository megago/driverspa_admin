package com.driverspa.util.otto.ws;

public class AdminStaffListRequestEvent {
     private String washerId;

	 public AdminStaffListRequestEvent(String washerId){
    	 this.washerId = washerId;
     }

    public String getWasherId() {
		return washerId;
	}

	public void setWasherId(String washerId) {
		this.washerId = washerId;
	}

}
