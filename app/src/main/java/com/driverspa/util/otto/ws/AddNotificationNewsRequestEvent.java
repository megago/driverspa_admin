package com.driverspa.util.otto.ws;

import com.driverspa.model.api.request.AdminNotificationNewsRequest;

public class AddNotificationNewsRequestEvent {

	private AdminNotificationNewsRequest request;

	public AddNotificationNewsRequestEvent(AdminNotificationNewsRequest request) {
		this.request = request;
	}

	public AdminNotificationNewsRequest getRequest() {
		return request;
	}

	public void setRequest(AdminNotificationNewsRequest request) {
		this.request = request;
	}
}
