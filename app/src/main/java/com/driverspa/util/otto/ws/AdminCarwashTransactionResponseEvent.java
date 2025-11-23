package com.driverspa.util.otto.ws;


import com.driverspa.model.api.response.AdminCarwashTransactionResponseHolder;

public class AdminCarwashTransactionResponseEvent {
	AdminCarwashTransactionResponseHolder data;

	public AdminCarwashTransactionResponseEvent(AdminCarwashTransactionResponseHolder data) {
		this.data = data;
	}

	public AdminCarwashTransactionResponseHolder getData() {
		return data;
	}

	public void setData(AdminCarwashTransactionResponseHolder data) {
		this.data = data;
	}
}
