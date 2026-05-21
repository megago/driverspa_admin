package com.driverspa.util.otto.ws;


import java.time.Instant;

public class AdminCampaignChangeRequestEvent {
     private String washerId;
     private String campaignId;
     private String campaignType;
	 private String description;
	 private Integer campaignDiscount;
	 private Instant endDate;

	public AdminCampaignChangeRequestEvent(String washerId, String campaignId, String campaignType, String description, Integer campaignDiscount, Instant endDate) {
		this.washerId = washerId;
		this.campaignId = campaignId;
		this.campaignType = campaignType;
		this.description = description;
		this.campaignDiscount = campaignDiscount;
		this.endDate = endDate;
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

	public String getCampaignType() {
		return campaignType;
	}

	public void setCampaignType(String campaignType) {
		this.campaignType = campaignType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getCampaignDiscount() {
		return campaignDiscount;
	}

	public void setCampaignDiscount(Integer campaignDiscount) {
		this.campaignDiscount = campaignDiscount;
	}

	public Instant getEndDate() {
		return endDate;
	}

	public void setEndDate(Instant endDate) {
		this.endDate = endDate;
	}
}
