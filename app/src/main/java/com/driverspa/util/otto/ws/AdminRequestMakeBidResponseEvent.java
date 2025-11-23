package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.BaseResponseHolder;

public class AdminRequestMakeBidResponseEvent {

     private BaseResponseHolder response;

	public AdminRequestMakeBidResponseEvent(BaseResponseHolder response) {
		this.response = response;
	}

	public BaseResponseHolder getResponse() {
		return response;
	}
}
