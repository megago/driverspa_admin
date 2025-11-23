package com.driverspa.model.api.request;

import com.google.gson.annotations.SerializedName;

public class WashCountCampaignOperationRequest {


	@SerializedName("description")
	String description;
	@SerializedName("wash_count")
	Integer washCount;

	public WashCountCampaignOperationRequest(String description, Integer washCount) {
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
}
