package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.ClientResponseHolder;

public class AdminClientResponseEvent {

	ClientResponseHolder data;

	public AdminClientResponseEvent(ClientResponseHolder data) {
		this.data = data;
	}

   public ClientResponseHolder getResult(){
	   return this.data;
   }
   
}
