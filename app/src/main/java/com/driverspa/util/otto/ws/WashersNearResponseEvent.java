package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.WashersResponseHolder;


public class WashersNearResponseEvent {

	WashersResponseHolder data;
	public WashersNearResponseEvent(WashersResponseHolder data) {
		this.data = data;
//		super(washers);
	}

	public WashersResponseHolder getData() {
		return data;
	}
}
