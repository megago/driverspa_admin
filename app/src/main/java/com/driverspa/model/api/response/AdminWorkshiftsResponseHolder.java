package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

public class AdminWorkshiftsResponseHolder extends BaseResponse {

	@SerializedName("data")
	private AdminWorkshiftsResponse response;

	public AdminWorkshiftsResponse getResponse() {
		return response;
	}

	public void setResponse(AdminWorkshiftsResponse response) {
		this.response = response;
	}

}

