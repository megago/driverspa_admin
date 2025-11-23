package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminCampaignResponseHolder;

public class AdminCampaignChangeResponseEvent {

	AdminCampaignResponseHolder data;

	public AdminCampaignChangeResponseEvent(AdminCampaignResponseHolder data){
		this.data = data;
	}
	
	public AdminCampaignResponseHolder getData() {
		return data;
	}

}
