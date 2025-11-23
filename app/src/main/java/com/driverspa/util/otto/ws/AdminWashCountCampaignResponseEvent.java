package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminWashCountCampaignResponseHolder;

public class AdminWashCountCampaignResponseEvent {

	AdminWashCountCampaignResponseHolder data;

	public AdminWashCountCampaignResponseEvent(AdminWashCountCampaignResponseHolder data){
		this.data = data;
	}
	
	public AdminWashCountCampaignResponseHolder getData() {
		return data;
	}

}
