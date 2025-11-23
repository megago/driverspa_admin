package com.driverspa.util.otto.ws;


import com.driverspa.model.api.response.AdminStaffResponseHolder;

public class AdminStaffResponseEvent {
	AdminStaffResponseHolder data;

	public AdminStaffResponseEvent(AdminStaffResponseHolder data) {
		this.data = data;
	}

	public AdminStaffResponseHolder getData() {
		return data;
	}

	public void setData(AdminStaffResponseHolder data) {
		this.data = data;
	}
}
