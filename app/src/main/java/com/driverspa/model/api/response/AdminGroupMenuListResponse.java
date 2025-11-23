package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import com.driverspa.model.Washer;

public class AdminGroupMenuListResponse extends BaseResponse {

	@SerializedName("objects")
	ArrayList<Washer.GroupMenu> groupMenuList;

	public ArrayList<Washer.GroupMenu> getGroupMenuList(){
		return groupMenuList;
	}
	
}
