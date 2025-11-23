package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminGroupMenuResponseHolder;

public class AdminGroupMenuResponseEvent {

	AdminGroupMenuResponseHolder data;

	public AdminGroupMenuResponseEvent(AdminGroupMenuResponseHolder data){
		this.data = data;
	}
	
	public AdminGroupMenuResponseHolder getData() {
		return data;
	}

}
