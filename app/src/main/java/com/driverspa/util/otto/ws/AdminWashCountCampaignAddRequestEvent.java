package com.driverspa.util.otto.ws;


import com.google.gson.annotations.SerializedName;

public class AdminWashCountCampaignAddRequestEvent {

	String washerId;


	@SerializedName("description")
	String description;


	@SerializedName("wash_count")
	Integer washCount;

	public AdminWashCountCampaignAddRequestEvent(String washerId, String description, Integer washCount) {
		this.washerId = washerId;
		this.description = description;
		this.washCount = washCount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getWashCount() {
		return washCount;
	}

	public void setWashCount(Integer washCount) {
		this.washCount = washCount;
	}

	public String getWasherId() {
		return washerId;
	}

	public void setWasherId(String washerId) {
		this.washerId = washerId;
	}
}
