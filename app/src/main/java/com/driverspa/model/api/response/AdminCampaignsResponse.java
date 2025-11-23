package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import com.driverspa.model.Campaign;

public class AdminCampaignsResponse extends BaseResponse {

	@SerializedName("objects")
	ArrayList<Campaign> campaigns;

	public ArrayList<Campaign> getCampaigns() {
		return campaigns;
	}

	public void setCampaigns(ArrayList<Campaign> campaigns) {
		this.campaigns = campaigns;
	}
}
