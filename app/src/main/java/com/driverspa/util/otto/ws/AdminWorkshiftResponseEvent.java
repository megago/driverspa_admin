package com.driverspa.util.otto.ws;


import com.driverspa.model.api.response.AdminWorkshiftResponseHolder;

public class AdminWorkshiftResponseEvent {
	AdminWorkshiftResponseHolder data;

	public AdminWorkshiftResponseEvent(AdminWorkshiftResponseHolder data) {
		this.data = data;
	}

	public AdminWorkshiftResponseHolder getData() {
		return data;
	}

	public void setData(AdminWorkshiftResponseHolder data) {
		this.data = data;
	}
}
