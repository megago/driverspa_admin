package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

import com.driverspa.model.CarwashFinancialStatistics;

public class AdminFinancialStatisticResponseHolder extends BaseResponseHolder {

	@SerializedName("data")
	private CarwashFinancialStatistics response;

	public CarwashFinancialStatistics getResponse() {
		return response;
	}

	public void setResponse(CarwashFinancialStatistics response) {
		this.response = response;
	}	 
}
