package com.driverspa.util.otto.ws;


import java.util.List;

import com.driverspa.model.BookingType;
import com.driverspa.model.Box;

public class AdminBoxTypeChangeRequestEvent extends Box {

   String washerId;

	public AdminBoxTypeChangeRequestEvent(String washerId, String boxId, BookingType boxType, String boxName, String washerPerson, List<String> defaultStaffs) {
		this.washerId = washerId;
		super.uid = boxId;
		super.bookingType = boxType;
		super.boxName = boxName;
		super.washerPerson = washerPerson;
		super.defualtStaffsId = defaultStaffs;
	}

	public String getWasherId() {
		return washerId;
	}
}
