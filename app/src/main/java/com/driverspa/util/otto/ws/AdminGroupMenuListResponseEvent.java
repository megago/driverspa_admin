package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.WasherResponseHolder;

public class AdminGroupMenuListResponseEvent {

	WasherResponseHolder data;

	public AdminGroupMenuListResponseEvent(WasherResponseHolder data){
		this.data = data;
	}
	
	public WasherResponseHolder getData() {
		return data;
	}

}
