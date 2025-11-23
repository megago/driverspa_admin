package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.BookResponseHolder;

public class AdminMarkAsClientPaidResponseEvent {

	BookResponseHolder data;

	public AdminMarkAsClientPaidResponseEvent(BookResponseHolder data) {
		this.data = data;
	}

   public BookResponseHolder getResult(){
	   return this.data;
   }
   
}
