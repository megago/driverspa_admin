package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

import com.driverspa.model.Workshift;

public class AdminWorkshiftResponseHolder extends BaseResponseHolder {

	@SerializedName("data")
	private Workshift response;

	public Workshift getResponse() {
		return response;
	}

	public void setResponse(Workshift response) {
		this.response = response;
	}

}
