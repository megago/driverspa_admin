package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.BookResponseHolder;

public class AdminBookResponseEvent {

	BookResponseHolder data;

	public AdminBookResponseEvent(BookResponseHolder data) {
		this.data = data;
	}

   public BookResponseHolder getResult(){
	   return this.data;
   }
   
}
