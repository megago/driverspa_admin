package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

import com.driverspa.model.CarwashCashFinancial;

public class AdminCashFinancialResponseHolder extends BaseResponseHolder {

	@SerializedName("data")
	private CarwashCashFinancial response;

	public CarwashCashFinancial getResponse() {
		return response;
	}

	public void setResponse(CarwashCashFinancial response) {
		this.response = response;
	}	 
}
