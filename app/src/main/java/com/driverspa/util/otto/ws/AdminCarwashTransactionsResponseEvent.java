package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminCarwashTransactionsResponseHolder;

public class AdminCarwashTransactionsResponseEvent {

	AdminCarwashTransactionsResponseHolder data;

	public AdminCarwashTransactionsResponseEvent(AdminCarwashTransactionsResponseHolder data){
		this.data = data;
	}
	
	public AdminCarwashTransactionsResponseHolder getData() {
		return data;
	}

}
