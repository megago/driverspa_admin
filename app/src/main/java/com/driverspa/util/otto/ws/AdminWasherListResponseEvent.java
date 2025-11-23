package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.BaseResponseHolder;
import com.driverspa.model.api.response.WashersResponseHolder;


public class AdminWasherListResponseEvent extends WasherBaseResponseEvent {

	BaseResponseHolder response;
	
	public AdminWasherListResponseEvent(WashersResponseHolder response) {
	    super(response.getResponse() !=null?response.getResponse().getResult():null);			
		this.response = response;
	}
	
	public BaseResponseHolder getResponse(){
		return this.response;
	}

}
