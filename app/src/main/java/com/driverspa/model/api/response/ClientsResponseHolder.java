package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

public class ClientsResponseHolder extends BaseResponseHolder {

	@SerializedName("data")
	private ClientsResponse response;

	public ClientsResponse getResponse() {
		return response;
	}

	public void setResponse(ClientsResponse response) {
		this.response = response;
	}

}
