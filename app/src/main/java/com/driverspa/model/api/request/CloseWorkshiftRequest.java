package com.driverspa.model.api.request;


public class CloseWorkshiftRequest {

	private String id;

	public CloseWorkshiftRequest(String id) {
		this.id = id;
	}

	public String getCarwash() {
		return id;
	}
	public void setCarwash(String id) {
		this.id = id;
	}
}
