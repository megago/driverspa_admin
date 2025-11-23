package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.ClientsResponseHolder;

public class AdminClientsResponseEvent {

	ClientsResponseHolder data;

	public AdminClientsResponseEvent(ClientsResponseHolder data) {
		this.data = data;
	}

   public ClientsResponseHolder getResult(){
	   return this.data;
   }
   
}
