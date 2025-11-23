package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.QueuedBooksResponseHolder;

public class AdminQueuedBooksResponseEvent {

	QueuedBooksResponseHolder data;
	public AdminQueuedBooksResponseEvent(QueuedBooksResponseHolder data) {
		this.data = data;
	}
   
	public QueuedBooksResponseHolder getResult(){
		return this.data;
	}
	
}
