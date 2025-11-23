package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminStaffResponseHolder;

public class AdminStaffAddResponseEvent {

	AdminStaffResponseHolder data;

	public AdminStaffAddResponseEvent(AdminStaffResponseHolder data){
		this.data = data;
	}
	
	public AdminStaffResponseHolder getData() {
		return data;
	}

}
