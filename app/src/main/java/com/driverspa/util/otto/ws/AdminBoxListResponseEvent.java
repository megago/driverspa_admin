package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminBoxesResponseHolder;

public class AdminBoxListResponseEvent {
   
	AdminBoxesResponseHolder data;
	
	public AdminBoxListResponseEvent(AdminBoxesResponseHolder data){
		this.data = data;
	}
	
	public AdminBoxesResponseHolder getData() {
		return data;
	}

}
