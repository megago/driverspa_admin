package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.BookResponseHolder;

public class AdminBookCancelResponseEvent {
	
	BookResponseHolder data;

	public AdminBookCancelResponseEvent(BookResponseHolder data) {
		this.data = data;
	}

	public BookResponseHolder getResult(){
		return data;
	}
	
}
