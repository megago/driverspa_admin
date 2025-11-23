package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

public class AdminGroupMenuListResponseHolder extends BaseResponseHolder {
	
	@SerializedName("data")
	private AdminGroupMenuListResponse response;

	public AdminGroupMenuListResponse getResponse() {
		return response;
	}

	public void setResponse(AdminGroupMenuListResponse response) {
		this.response = response;
	}	 
}
