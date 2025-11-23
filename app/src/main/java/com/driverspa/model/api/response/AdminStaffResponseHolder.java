package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

import com.driverspa.model.Staff;

public class AdminStaffResponseHolder extends BaseResponseHolder {

	@SerializedName("data")
	private Staff response;

	public Staff getResponse() {
		return response;
	}

	public void setResponse(Staff response) {
		this.response = response;
	}	 
}
