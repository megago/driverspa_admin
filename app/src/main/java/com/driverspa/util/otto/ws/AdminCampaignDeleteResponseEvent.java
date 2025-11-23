package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminCampaignResponseHolder;

public class AdminCampaignDeleteResponseEvent {

	AdminCampaignResponseHolder data;

	public AdminCampaignDeleteResponseEvent(AdminCampaignResponseHolder data){
		this.data = data;
	}
	
	public AdminCampaignResponseHolder getData() {
		return data;
	}

}
