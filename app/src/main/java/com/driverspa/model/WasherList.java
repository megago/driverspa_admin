package com.driverspa.model;

import com.google.gson.Gson;

import java.util.List;

public class WasherList extends WasherPublic {

	List<WasherPublic> washerList;

	public WasherList(List<WasherPublic> washerList) {
		this.washerList = washerList;
	}

	public List<WasherPublic> getWasherList() {
		return washerList;
	}

	public void setWasherList(List<WasherPublic> washerList) {
		this.washerList = washerList;
	}

	// GSON
	public String serialize() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public static WasherList deserialize(String serializedData) {
		Gson gson = new Gson();
		return gson.fromJson(serializedData, WasherList.class);
	}
}
