package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminCampaignResponseHolder;

public class AdminCampaignResponseEvent {

	AdminCampaignResponseHolder data;

	public AdminCampaignResponseEvent(AdminCampaignResponseHolder data){
		this.data = data;
	}
	
	public AdminCampaignResponseHolder getData() {
		return data;
	}

}
