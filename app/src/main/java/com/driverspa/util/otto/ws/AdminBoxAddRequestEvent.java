package com.driverspa.util.otto.ws;


public class AdminBoxAddRequestEvent {
     private String washerId;
     private String boxType;
	 private String boxName;
	 private String washerPerson;

	public AdminBoxAddRequestEvent(String washerId, String boxType, String boxName, String washerPerson) {
		this.washerId = washerId;
		this.boxType = boxType;
		this.boxName = boxName;
		this.washerPerson = washerPerson;
	}

	public String getWasherPerson() {
		return washerPerson;
	}

	public void setWasherPerson(String washerPerson) {
		this.washerPerson = washerPerson;
	}

	public void setWasherId(String washerId) {
		this.washerId = washerId;
	}

	public void setBoxType(String boxType) {
		this.boxType = boxType;
	}

	public String getBoxName() {
		return boxName;
	}

	public void setBoxName(String boxName) {
		this.boxName = boxName;
	}

	public String getWasherId() {
		return washerId;
	}

	
	public String getBoxType(){
		return this.boxType;
	}



}
