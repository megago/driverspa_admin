package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.BookResponseHolder;

public class AdminBookApproveResponseEvent {

	

	private BookResponseHolder data;
	
	public AdminBookApproveResponseEvent(BookResponseHolder data) {
		this.data = data;
	}

	public BookResponseHolder getResult() {
		return data;
	}

}
