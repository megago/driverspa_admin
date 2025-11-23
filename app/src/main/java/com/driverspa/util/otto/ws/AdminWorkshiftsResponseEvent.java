package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminWorkshiftsResponseHolder;

public class AdminWorkshiftsResponseEvent {

	AdminWorkshiftsResponseHolder data;

	public AdminWorkshiftsResponseEvent(AdminWorkshiftsResponseHolder data){
		this.data = data;
	}
	
	public AdminWorkshiftsResponseHolder getData() {
		return data;
	}

}
