package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminAnalyticsResponseHolder;

public class AdminAnalyticsResponseEvent {

	AdminAnalyticsResponseHolder data;

	public AdminAnalyticsResponseEvent(AdminAnalyticsResponseHolder data) {
		this.data = data;
	}

   public AdminAnalyticsResponseHolder getResult(){
	   return this.data;
   }
   
}
