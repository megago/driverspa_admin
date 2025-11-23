package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.BookResponseHolder;

public class AdminPostponeQueueResponseEvent {

	BookResponseHolder data;

	public AdminPostponeQueueResponseEvent(BookResponseHolder data) {
		this.data = data;
	}

   public BookResponseHolder getResult(){
	   return this.data;
   }
   
}
