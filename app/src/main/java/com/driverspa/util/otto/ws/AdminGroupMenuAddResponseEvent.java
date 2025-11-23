package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminGroupMenuResponseHolder;

public class AdminGroupMenuAddResponseEvent {

	AdminGroupMenuResponseHolder data;

	public AdminGroupMenuAddResponseEvent(AdminGroupMenuResponseHolder data){
		this.data = data;
	}
	
	public AdminGroupMenuResponseHolder getData() {
		return data;
	}

}
