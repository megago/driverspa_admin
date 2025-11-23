package com.driverspa.model.api.request;

import com.google.gson.annotations.SerializedName;

public class BoxOperationRequest {

	@SerializedName("box_name")
	private String boxName;
	@SerializedName("washer_person")
	private String washerPerson;

	@SerializedName("booking_type")
	private String bookingType;

	public BoxOperationRequest(String boxName, String bookingType, String washerPerson) {
		this.boxName = boxName;
		this.bookingType = bookingType;
		this.washerPerson = washerPerson;
	}

	public BoxOperationRequest( String bookingType) {
		this.bookingType = bookingType;
	}

	public String getBookingType(){
		return bookingType;
	}

	public void setBookingType(String bookingType){
		this.bookingType = bookingType;
	}


	public String getBoxName() {
		return boxName;
	}

	public void setBoxName(String boxName) {
		this.boxName = boxName;
	}
}
