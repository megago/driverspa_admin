package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.BookResponseHolder;

public class AdminBookNoClientResponseEvent {

	
	private BookResponseHolder data;

	public AdminBookNoClientResponseEvent(BookResponseHolder data) {
		this.data = data;
	}

	public BookResponseHolder getResult() {
		return data;
	}

}
