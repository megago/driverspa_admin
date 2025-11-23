package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

import com.driverspa.model.WashCountCampaign;

public class AdminWashCountCampaignResponseHolder extends BaseResponseHolder {

	@SerializedName("data")
	private WashCountCampaign response;

	public WashCountCampaign getResponse() {
		return response;
	}

	public void setResponse(WashCountCampaign response) {
		this.response = response;
	}	 
}
