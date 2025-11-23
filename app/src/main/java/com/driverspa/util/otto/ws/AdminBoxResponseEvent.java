package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminBoxResponseHolder;

public class AdminBoxResponseEvent {
   
	AdminBoxResponseHolder data;
	
	public AdminBoxResponseEvent(AdminBoxResponseHolder data){
		this.data = data;
	}
	
	public AdminBoxResponseHolder getData() {
		return data;
	}

}
