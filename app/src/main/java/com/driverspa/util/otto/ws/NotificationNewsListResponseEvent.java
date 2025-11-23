package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.NotificationNewsResponseHolder;

public class NotificationNewsListResponseEvent {

	NotificationNewsResponseHolder data;
	public NotificationNewsListResponseEvent(NotificationNewsResponseHolder data) {
		this.data = data;
	}
   
	public NotificationNewsResponseHolder getData(){
		return this.data;
	}
	
}
