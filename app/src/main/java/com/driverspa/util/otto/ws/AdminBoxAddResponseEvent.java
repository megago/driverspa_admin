package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminBoxResponseHolder;

public class AdminBoxAddResponseEvent {
   
	AdminBoxResponseHolder data;
	
	public AdminBoxAddResponseEvent(AdminBoxResponseHolder data){
		this.data = data;
	}
	
	public AdminBoxResponseHolder getData() {
		return data;
	}

}
