package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import com.driverspa.model.AdminNotificationNews;

public class NotificationNewsResponse extends BaseResponse {

	@SerializedName("objects")
	private ArrayList<AdminNotificationNews> result;
	private ListMetaDataResponse meta;

	public ArrayList<AdminNotificationNews> getResult() {
		return result;
	}

	public ListMetaDataResponse getMeta() {
		return meta;
	}
		
}
