package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminGroupMenuResponseHolder;

public class AdminGroupMenuDeleteResponseEvent {

	AdminGroupMenuResponseHolder data;

	public AdminGroupMenuDeleteResponseEvent(AdminGroupMenuResponseHolder data){
		this.data = data;
	}
	
	public AdminGroupMenuResponseHolder getData() {
		return data;
	}

}
