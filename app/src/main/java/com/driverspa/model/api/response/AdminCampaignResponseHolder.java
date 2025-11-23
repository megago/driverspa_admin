package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

import com.driverspa.model.Campaign;

public class AdminCampaignResponseHolder extends BaseResponseHolder {

	@SerializedName("data")
	private Campaign response;

	public Campaign getResponse() {
		return response;
	}

	public void setResponse(Campaign response) {
		this.response = response;
	}	 
}
