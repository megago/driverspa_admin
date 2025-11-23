package com.driverspa.model.api.request;


public class AddWorkshiftRequest {

	private String carwash;

	public AddWorkshiftRequest(String carwash) {
		this.carwash = carwash;
	}

	public String getCarwash() {
		return carwash;
	}
	public void setCarwash(String carwash) {
		this.carwash = carwash;
	}
}
