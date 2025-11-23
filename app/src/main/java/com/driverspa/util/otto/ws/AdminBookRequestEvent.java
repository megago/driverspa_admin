package com.driverspa.util.otto.ws;

import com.driverspa.model.api.request.BookingRequest;

public class AdminBookRequestEvent {

     private BookingRequest bookingRequest;

     public AdminBookRequestEvent(BookingRequest bookingRequest){
    	 this.bookingRequest = bookingRequest; 
     }
	 public BookingRequest getBookingRequest() {
		return bookingRequest;
	}

	public void setBookingRequest(BookingRequest bookingRequest) {
		this.bookingRequest = bookingRequest;
	}

}
