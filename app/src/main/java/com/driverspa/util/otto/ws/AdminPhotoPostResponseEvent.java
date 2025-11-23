package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.BaseResponseHolder;

public class AdminPhotoPostResponseEvent {
	BaseResponseHolder response;

	public AdminPhotoPostResponseEvent(BaseResponseHolder response) {
		this.response = response;
	}

	public BaseResponseHolder getResponse(){
		return this.response;
	}
}
