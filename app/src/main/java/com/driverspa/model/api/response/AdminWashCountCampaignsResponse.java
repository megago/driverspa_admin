package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import com.driverspa.model.WashCountCampaign;

public class AdminWashCountCampaignsResponse extends BaseResponse {

	@SerializedName("objects")
	ArrayList<WashCountCampaign> campaigns;

	public ArrayList<WashCountCampaign> getCampaigns() {
		return campaigns;
	}

	public void setCampaigns(ArrayList<WashCountCampaign> campaigns) {
		this.campaigns = campaigns;
	}
}
