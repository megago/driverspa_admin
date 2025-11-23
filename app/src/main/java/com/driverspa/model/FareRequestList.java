package com.driverspa.model;

import com.google.gson.Gson;

import java.util.List;

public class FareRequestList {

	List<FareRequest> fareRequests;

	public List<FareRequest> getFareRequests() {
		return fareRequests;
	}

	public void setFareRequests(List<FareRequest> fareRequests) {
		this.fareRequests = fareRequests;
	}

	// GSON
	public String serialize() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public static FareRequestList deserialize(String serializedData) {
		Gson gson = new Gson();
		return gson.fromJson(serializedData, FareRequestList.class);
	}
}
