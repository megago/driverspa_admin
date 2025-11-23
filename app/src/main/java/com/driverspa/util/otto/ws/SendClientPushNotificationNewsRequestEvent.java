package com.driverspa.util.otto.ws;

import com.driverspa.model.api.request.AdminNotificationNewsRequest;

public class SendClientPushNotificationNewsRequestEvent {

	private AdminNotificationNewsRequest request;

	public SendClientPushNotificationNewsRequestEvent(AdminNotificationNewsRequest request) {
		this.request = request;
	}

	public AdminNotificationNewsRequest getRequest() {
		return request;
	}

	public void setRequest(AdminNotificationNewsRequest request) {
		this.request = request;
	}
}
