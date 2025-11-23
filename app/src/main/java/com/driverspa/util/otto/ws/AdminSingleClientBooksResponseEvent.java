package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.SingleClientBooksResponseHolder;

public class AdminSingleClientBooksResponseEvent {

	SingleClientBooksResponseHolder data;
	public AdminSingleClientBooksResponseEvent(SingleClientBooksResponseHolder data) {
		this.data = data;
	}
   
	public SingleClientBooksResponseHolder getResult(){
		return this.data;
	}
	
}
