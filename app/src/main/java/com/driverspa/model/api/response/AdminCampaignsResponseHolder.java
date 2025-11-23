package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

public class AdminCampaignsResponseHolder extends BaseResponseHolder {
	
	@SerializedName("data")
	private AdminCampaignsResponse response;

	public AdminCampaignsResponse getResponse() {
		return response;
	}

	public void setResponse(AdminCampaignsResponse response) {
		this.response = response;
	}	 
}
