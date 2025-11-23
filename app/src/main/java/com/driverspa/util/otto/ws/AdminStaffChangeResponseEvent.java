package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminStaffResponseHolder;

public class AdminStaffChangeResponseEvent {

	AdminStaffResponseHolder data;

	public AdminStaffChangeResponseEvent(AdminStaffResponseHolder data){
		this.data = data;
	}
	
	public AdminStaffResponseHolder getData() {
		return data;
	}

}
