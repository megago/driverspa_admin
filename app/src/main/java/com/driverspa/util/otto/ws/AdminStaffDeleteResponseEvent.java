package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminStaffResponseHolder;

public class AdminStaffDeleteResponseEvent {

	AdminStaffResponseHolder data;

	public AdminStaffDeleteResponseEvent(AdminStaffResponseHolder data){
		this.data = data;
	}
	
	public AdminStaffResponseHolder getData() {
		return data;
	}

}
