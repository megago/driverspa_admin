package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.BookResponseHolder;

public class AdminFinishQueueResponseEvent {

	BookResponseHolder data;

	public AdminFinishQueueResponseEvent(BookResponseHolder data) {
		this.data = data;
	}

   public BookResponseHolder getResult(){
	   return this.data;
   }
   
}
