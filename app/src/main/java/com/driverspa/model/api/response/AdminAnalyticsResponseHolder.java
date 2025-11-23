package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

public class AdminAnalyticsResponseHolder extends BaseResponseHolder {

	@SerializedName("data")
	private AdminAnalyticsResponse data;

	public AdminAnalyticsResponse getResponse() {
		return data;
	}

	public void setResponse(AdminAnalyticsResponse data) {
		this.data = data;
	}
}
