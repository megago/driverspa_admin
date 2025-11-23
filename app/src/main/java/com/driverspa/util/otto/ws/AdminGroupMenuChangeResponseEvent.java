package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminGroupMenuResponseHolder;

public class AdminGroupMenuChangeResponseEvent {

	AdminGroupMenuResponseHolder data;

	public AdminGroupMenuChangeResponseEvent(AdminGroupMenuResponseHolder data){
		this.data = data;
	}
	
	public AdminGroupMenuResponseHolder getData() {
		return data;
	}

}
