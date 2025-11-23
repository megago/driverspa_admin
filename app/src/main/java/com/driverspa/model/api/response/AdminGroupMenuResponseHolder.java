package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

import com.driverspa.model.Washer;

public class AdminGroupMenuResponseHolder extends BaseResponseHolder {

	@SerializedName("data")
	private Washer.GroupMenu response;

	public Washer.GroupMenu getResponse() {
		return response;
	}

	public void setResponse(Washer.GroupMenu response) {
		this.response = response;
	}	 
}
