package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

public class AdminStaffsResponseHolder extends BaseResponse {
	
	@SerializedName("data")
	private AdminStaffsResponse response;

	public AdminStaffsResponse getResponse() {
		return response;
	}

	public void setResponse(AdminStaffsResponse response) {
		this.response = response;
	}	 
}
