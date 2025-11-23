package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminCarwashTransactionResponseHolder;

public class AdminCarwashTransactionAddResponseEvent {

	AdminCarwashTransactionResponseHolder data;

	public AdminCarwashTransactionAddResponseEvent(AdminCarwashTransactionResponseHolder data){
		this.data = data;
	}
	
	public AdminCarwashTransactionResponseHolder getData() {
		return data;
	}

}
