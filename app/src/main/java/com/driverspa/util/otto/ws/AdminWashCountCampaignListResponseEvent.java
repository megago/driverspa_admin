package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminWashCountCampaignsResponseHolder;

public class AdminWashCountCampaignListResponseEvent {

	AdminWashCountCampaignsResponseHolder data;

	public AdminWashCountCampaignListResponseEvent(AdminWashCountCampaignsResponseHolder data){
		this.data = data;
	}
	
	public AdminWashCountCampaignsResponseHolder getData() {
		return data;
	}

}
