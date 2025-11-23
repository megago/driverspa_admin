package com.driverspa.util.otto.ws;

public class AdminWashCountCampaignListRequestEvent {
     private String washerId;

	 public AdminWashCountCampaignListRequestEvent(String washerId){
    	 this.washerId = washerId;
     }
	
    public String getWasherId() {
		return washerId;
	}

	public void setWasherId(String washerId) {
		this.washerId = washerId;
	}

}
