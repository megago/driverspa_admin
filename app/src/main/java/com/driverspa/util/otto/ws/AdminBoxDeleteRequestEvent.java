package com.driverspa.util.otto.ws;


public class AdminBoxDeleteRequestEvent {
     private String washerId;
     private String boxId;
	 public AdminBoxDeleteRequestEvent(String washerId,String boxId){
    	 this.washerId = washerId;
    	 this.boxId = boxId;
     }
	
    public String getWasherId() {
		return washerId;
	}

	public void setWasherId(String washerId) {
		this.washerId = washerId;
	}

	public String getBoxId(){
		return this.boxId;
	}
}
