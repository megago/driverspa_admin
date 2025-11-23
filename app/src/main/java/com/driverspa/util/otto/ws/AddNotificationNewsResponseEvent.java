package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.BaseResponseHolder;

public class AddNotificationNewsResponseEvent {

	BaseResponseHolder data;
	public AddNotificationNewsResponseEvent(BaseResponseHolder data) {
		this.data = data;
	}

	public BaseResponseHolder getData(){
		return this.data;
	}

}
