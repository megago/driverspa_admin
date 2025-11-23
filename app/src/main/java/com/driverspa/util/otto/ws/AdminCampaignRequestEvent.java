package com.driverspa.util.otto.ws;


public class AdminCampaignRequestEvent {
     private String washerId;
     private String campaignId;

	public AdminCampaignRequestEvent(String washerId, String campaignId) {
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
