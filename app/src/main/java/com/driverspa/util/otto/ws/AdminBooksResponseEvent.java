package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.BooksResponseHolder;

public class AdminBooksResponseEvent extends BooksBaseResponseEvent {

	BooksResponseHolder data;
	public AdminBooksResponseEvent(BooksResponseHolder data) {
		super(data != null && data.getResponse()!=null?data.getResponse().getBookInfoList():null);
		this.data = data;
	}
   
	public BooksResponseHolder getResult(){
		return this.data;
	}
	
}
