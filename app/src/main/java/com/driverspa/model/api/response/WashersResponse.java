package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import com.driverspa.model.WasherPublic;

public class WashersResponse extends BaseResponse {

	@SerializedName("objects")
	private ArrayList<WasherPublic> result;
	private ListMetaDataResponse meta;
	
	public ArrayList<WasherPublic> getResult() {
		return result;
	}		
		
	public ListMetaDataResponse getMeta() {
		return meta;
	}
		
}
