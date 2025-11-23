package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.BookResponseHolder;

public class AdminBookFailResponseEvent {

	
	private BookResponseHolder data;

	public AdminBookFailResponseEvent(BookResponseHolder data) {
		this.data = data;
	}

	public BookResponseHolder getResult() {
		return data;
	}

}
