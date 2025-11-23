package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.BookInfoResponseHolder;

public class AdminBookInfoResponseEvent {

	BookInfoResponseHolder data;

	public AdminBookInfoResponseEvent(BookInfoResponseHolder data) {
		this.data = data;
	}

	public BookInfoResponseHolder getResult() {
		return this.data;
	}


}
