package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

public class NotificationNewsResponseHolder extends BaseResponseHolder {

	@SerializedName("data")
	private NotificationNewsResponse response;

	public NotificationNewsResponse getResponse() {
		return response;
	}
	
}
