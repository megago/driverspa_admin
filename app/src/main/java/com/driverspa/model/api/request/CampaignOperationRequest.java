package com.driverspa.model.api.request;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;

public class CampaignOperationRequest {

	@SerializedName("end_time")
	Instant endTime;

	@SerializedName("campaign_type")
	String campaignType;

	@SerializedName("description")
	String description;
	@SerializedName("ts")
	Instant ts;

	@SerializedName("campaign_discount")
	Integer campaignDiscount;


	public Instant getEndTime() {
		return endTime;
	}

	public void setEndTime(Instant endTime) {
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

	public Instant getTs() {
		return ts;
	}

	public void setTs(Instant ts) {
		this.ts = ts;
	}

	public Integer getCampaignDiscount() {
		return campaignDiscount;
	}

	public void setCampaignDiscount(Integer campaignDiscount) {
		this.campaignDiscount = campaignDiscount;
	}

	public CampaignOperationRequest(String description,String campaignType, Instant endTime, Integer campaignDiscount) {
		this.endTime = endTime;
		this.campaignType = campaignType;
		this.description = description;
		this.ts = ts;
		this.campaignDiscount = campaignDiscount;
	}
}
