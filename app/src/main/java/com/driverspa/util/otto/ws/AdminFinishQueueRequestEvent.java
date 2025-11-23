package com.driverspa.util.otto.ws;

import com.driverspa.model.api.request.BookingRequest;

public class AdminFinishQueueRequestEvent {

     private BookingRequest bookingRequest;

     public AdminFinishQueueRequestEvent(BookingRequest bookingRequest){
    	 this.bookingRequest = bookingRequest; 
     }
	 public BookingRequest getBookingRequest() {
		return bookingRequest;
	}

	public void setBookingRequest(BookingRequest bookingRequest) {
		this.bookingRequest = bookingRequest;
	}

}
