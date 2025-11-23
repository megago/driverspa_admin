package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminCampaignsResponseHolder;

public class AdminCampaignListResponseEvent {

	AdminCampaignsResponseHolder data;

	public AdminCampaignListResponseEvent(AdminCampaignsResponseHolder data){
		this.data = data;
	}
	
	public AdminCampaignsResponseHolder getData() {
		return data;
	}

}
