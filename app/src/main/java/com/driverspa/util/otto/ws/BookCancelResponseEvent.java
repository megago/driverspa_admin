package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.BookResponse;

public class BookCancelResponseEvent {

	
	private BookResponse response;

	public BookCancelResponseEvent(BookResponse bookResponse) {
		this.response = bookResponse;
	}

	public BookResponse getResponse() {
		return response;
	}

	public void setBookResponse(BookResponse response) {
		this.response = response;
	}

}
