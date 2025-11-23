package com.driverspa.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Campaign {
	@SerializedName("uid")
	String id;

	@SerializedName("end_time")
	Date endTime;

	@SerializedName("campaign_type")
	CampaignType campaignType;

	@SerializedName("description")
	String description;
	@SerializedName("ts")
	Date ts;

	@SerializedName("campaign_discount")
	Integer campaignDiscount;


	public CampaignType getCampaignType() {
		return campaignType;
	}

	public void setCampaignType(CampaignType campaignType) {
		this.campaignType = campaignType;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
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

	public Integer getCampaignDiscount() {
		return campaignDiscount;
	}

	public void setCampaignDiscount(Integer campaignDiscount) {
		this.campaignDiscount = campaignDiscount;
	}

	public void setTs(Date ts) {
		this.ts = ts;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	// GSON
	public String serialize() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public static Campaign deserialize(String serializedData) {
		Gson gson = new Gson();
		return gson.fromJson(serializedData, Campaign.class);
	}

}
