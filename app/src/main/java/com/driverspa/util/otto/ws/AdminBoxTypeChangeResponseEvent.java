package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminBoxResponseHolder;

public class AdminBoxTypeChangeResponseEvent {
   
	AdminBoxResponseHolder data;
	
	public AdminBoxTypeChangeResponseEvent(AdminBoxResponseHolder data){
		this.data = data;
	}
	
	public AdminBoxResponseHolder getData() {
		return data;
	}

}
