package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminWashCountCampaignResponseHolder;

public class AdminWashCountCampaignChangeResponseEvent {

	AdminWashCountCampaignResponseHolder data;

	public AdminWashCountCampaignChangeResponseEvent(AdminWashCountCampaignResponseHolder data){
		this.data = data;
	}
	
	public AdminWashCountCampaignResponseHolder getData() {
		return data;
	}

}
