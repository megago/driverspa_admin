package com.driverspa.util.otto.ws;


import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class AdminCampaignAddRequestEvent {

	String washerId;

	@SerializedName("campaign_type")
	String campaignType;

	@SerializedName("description")
	String description;

	@SerializedName("end_time")
	Date endTime;

	@SerializedName("campaign_discount")
	Integer campaignDiscount;

	public AdminCampaignAddRequestEvent(String washerId, String description, String campaignType, Date endTime, Integer campaignDiscount) {
		this.washerId = washerId;
		this.campaignType = campaignType;
		this.description = description;
		this.endTime = endTime;
		this.campaignDiscount = campaignDiscount;
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

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Integer getCampaignDiscount() {
		return campaignDiscount;
	}

	public void setCampaignDiscount(Integer campaignDiscount) {
		this.campaignDiscount = campaignDiscount;
	}

	public String getWasherId() {
		return washerId;
	}

	public void setWasherId(String washerId) {
		this.washerId = washerId;
	}
}
