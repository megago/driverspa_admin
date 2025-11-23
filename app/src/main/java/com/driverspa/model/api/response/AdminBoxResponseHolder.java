package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

import com.driverspa.model.Box;

public class AdminBoxResponseHolder extends BaseResponseHolder {
	
	@SerializedName("data")
	private Box response;

	public Box getResponse() {
		return response;
	}

	public void setResponse(Box response) {
		this.response = response;
	}	 
}
