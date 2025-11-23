package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.BaseResponseHolder;

public class AdminWashCountCampaignAddResponseEvent {

	BaseResponseHolder data;

	public AdminWashCountCampaignAddResponseEvent(BaseResponseHolder data){
		this.data = data;
	}
	
	public BaseResponseHolder getData() {
		return data;
	}

}
