package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.BookResponseHolder;

public class AdminBookStartResponseEvent {

	
	private BookResponseHolder data;

	public AdminBookStartResponseEvent(BookResponseHolder data) {
		this.data = data;
	}

	public BookResponseHolder getResult() {
		return data;
	}


}
