package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.WasherResponseHolder;

public class AdminWasherInfoResponseEvent {
   
	WasherResponseHolder data;
	
	public AdminWasherInfoResponseEvent(WasherResponseHolder data){
		this.data = data;
	}
	
	public WasherResponseHolder getData() {
		return data;
	}

}
