package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import com.driverspa.model.Staff;

public class AdminStaffsResponse extends BaseResponse {

	@SerializedName("objects")
	ArrayList<Staff> staffs;

	public ArrayList<Staff> getStaffs(){
		return staffs;
	}

}
