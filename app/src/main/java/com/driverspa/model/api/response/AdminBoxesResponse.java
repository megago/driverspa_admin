package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import com.driverspa.model.Box;

public class AdminBoxesResponse extends BaseResponse {

	@SerializedName("objects")
	ArrayList<Box> boxes;

	public ArrayList<Box> getBoxes(){
		return boxes;
	}
	
}
