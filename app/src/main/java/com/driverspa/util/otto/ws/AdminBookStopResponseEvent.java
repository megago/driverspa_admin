package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.BookResponseHolder;

public class AdminBookStopResponseEvent {

	
	private BookResponseHolder data;

	public AdminBookStopResponseEvent(BookResponseHolder data) {
		this.data = data;
	}

	public BookResponseHolder getResult() {
		return data;
	}

}
