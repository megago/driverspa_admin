package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.ClientsResponseHolder;

public class AdminNotRegisteredClientsResponseEvent {

	ClientsResponseHolder data;

	public AdminNotRegisteredClientsResponseEvent(ClientsResponseHolder data) {
		this.data = data;
	}

   public ClientsResponseHolder getResult(){
	   return this.data;
   }
   
}
