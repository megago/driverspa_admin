package com.driverspa.util.otto.ws;

public class AdminCampaignListRequestEvent {
     private String washerId;

	 public AdminCampaignListRequestEvent(String washerId){
    	 this.washerId = washerId;
     }
	
    public String getWasherId() {
		return washerId;
	}

	public void setWasherId(String washerId) {
		this.washerId = washerId;
	}

}
