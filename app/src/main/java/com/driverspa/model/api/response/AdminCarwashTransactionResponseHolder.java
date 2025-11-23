package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

import com.driverspa.model.CarwashTransaction;

public class AdminCarwashTransactionResponseHolder extends BaseResponseHolder {

	@SerializedName("data")
	private CarwashTransaction response;

	public CarwashTransaction getResponse() {
		return response;
	}

	public void setResponse(CarwashTransaction response) {
		this.response = response;
	}	 
}
