package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminBoxResponseHolder;

public class AdminBoxDeleteResponseEvent {
   
	AdminBoxResponseHolder data;
	
	public AdminBoxDeleteResponseEvent(AdminBoxResponseHolder data){
		this.data = data;
	}
	
	public AdminBoxResponseHolder getData() {
		return data;
	}

}
