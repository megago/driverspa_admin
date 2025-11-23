package com.driverspa.model;

import com.google.gson.annotations.SerializedName;

import com.driverspa.util.Constants;

public enum BookingType {

	@SerializedName("online")
	Online("online"),

	@SerializedName("offline")
	Offline("offline"),

	@SerializedName("hybrid")
	Hybrid("hybrid"),

	Unknown("unknown");

	public static BookingType toEnum (String enumString) {
		try {
			if(Constants.ONLINE.equals(enumString))
				return Online;
			else if(Constants.OFFLINE.equals(enumString))
				return Offline;
			else if(Constants.HYBRID.equals(enumString))
				return Hybrid;
			else
			return Unknown;
//			return valueOf(enumString);
		} catch (Exception ex) {
			// For error cases
			return Unknown;
		}
	}

	private final String id;

	BookingType(String id) { this.id = id;}

	public String getValue() { return id; }

	public String toString() {
		return getValue();
	}
	
}
