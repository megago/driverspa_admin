package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.QueuedBooksResponseHolder;

public class AdminStatisticsResponseEvent {

	QueuedBooksResponseHolder data;
	public AdminStatisticsResponseEvent(QueuedBooksResponseHolder data) {
		this.data = data;
	}
   
	public QueuedBooksResponseHolder getResult(){
		return this.data;
	}
	
}
