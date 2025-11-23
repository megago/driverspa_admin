package com.driverspa.util.otto.ws;


public class AdminCampaignDeleteRequestEvent {
     private String washerId;
     private String campaignId;
	 public AdminCampaignDeleteRequestEvent(String washerId, String campaignId){
    	 this.washerId = washerId;
    	 this.campaignId = campaignId;
     }
	
    public String getWasherId() {
		return washerId;
	}

	public void setWasherId(String washerId) {
		this.washerId = washerId;
	}

	public String getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(String campaignId) {
		this.campaignId = campaignId;
	}
}
