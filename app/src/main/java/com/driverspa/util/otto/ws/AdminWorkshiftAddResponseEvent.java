package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminWorkshiftResponseHolder;

public class AdminWorkshiftAddResponseEvent {

	AdminWorkshiftResponseHolder data;

	public AdminWorkshiftAddResponseEvent(AdminWorkshiftResponseHolder data){
		this.data = data;
	}
	
	public AdminWorkshiftResponseHolder getData() {
		return data;
	}

}
