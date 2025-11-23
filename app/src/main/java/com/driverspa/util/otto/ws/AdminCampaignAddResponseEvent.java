package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.BaseResponseHolder;

public class AdminCampaignAddResponseEvent {

	BaseResponseHolder data;

	public AdminCampaignAddResponseEvent(BaseResponseHolder data){
		this.data = data;
	}
	
	public BaseResponseHolder getData() {
		return data;
	}

}
