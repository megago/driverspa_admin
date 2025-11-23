package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminStaffsResponseHolder;

public class AdminStaffListResponseEvent {

	AdminStaffsResponseHolder data;

	public AdminStaffListResponseEvent(AdminStaffsResponseHolder data){
		this.data = data;
	}
	
	public AdminStaffsResponseHolder getData() {
		return data;
	}

}
