package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

import com.driverspa.model.CarwashStaffFinancialInfo;

public class AdminStaffFinancialResponseHolder extends BaseResponseHolder {

	@SerializedName("data")
	private CarwashStaffFinancialInfo response;

	public CarwashStaffFinancialInfo getResponse() {
		return response;
	}

	public void setResponse(CarwashStaffFinancialInfo response) {
		this.response = response;
	}	 
}
