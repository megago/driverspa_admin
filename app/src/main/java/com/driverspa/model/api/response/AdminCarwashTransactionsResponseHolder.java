package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

public class AdminCarwashTransactionsResponseHolder extends BaseResponseHolder {
	
	@SerializedName("data")
	private AdminCarwashTransactionsResponse response;

	public AdminCarwashTransactionsResponse getResponse() {
		return response;
	}

	public void setResponse(AdminCarwashTransactionsResponse response) {
		this.response = response;
	}	 
}
