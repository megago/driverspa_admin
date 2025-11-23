package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

public class AdminWashCountCampaignsResponseHolder extends BaseResponseHolder {
	
	@SerializedName("data")
	private AdminWashCountCampaignsResponse response;

	public AdminWashCountCampaignsResponse getResponse() {
		return response;
	}

	public void setResponse(AdminWashCountCampaignsResponse response) {
		this.response = response;
	}	 
}
