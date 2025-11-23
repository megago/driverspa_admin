package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminWashCountCampaignResponseHolder;

public class AdminWashCountCampaignDeleteResponseEvent {

	AdminWashCountCampaignResponseHolder data;

	public AdminWashCountCampaignDeleteResponseEvent(AdminWashCountCampaignResponseHolder data){
		this.data = data;
	}
	
	public AdminWashCountCampaignResponseHolder getData() {
		return data;
	}

}
