package com.driverspa.model.api.request;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class CampaignOperationRequest {

	@SerializedName("end_time")
	Date endTime;

	@SerializedName("campaign_type")
	String campaignType;

	@SerializedName("description")
	String description;
	@SerializedName("ts")
	Date ts;

	@SerializedName("campaign_discount")
	Integer campaignDiscount;


	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
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

	public Date getTs() {
		return ts;
	}

	public void setTs(Date ts) {
		this.ts = ts;
	}

	public Integer getCampaignDiscount() {
		return campaignDiscount;
	}

	public void setCampaignDiscount(Integer campaignDiscount) {
		this.campaignDiscount = campaignDiscount;
	}

	public CampaignOperationRequest(String description,String campaignType, Date endTime, Integer campaignDiscount) {
		this.endTime = endTime;
		this.campaignType = campaignType;
		this.description = description;
		this.ts = ts;
		this.campaignDiscount = campaignDiscount;
	}
}
