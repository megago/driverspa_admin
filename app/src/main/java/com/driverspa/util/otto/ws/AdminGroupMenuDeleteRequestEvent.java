package com.driverspa.util.otto.ws;


public class AdminGroupMenuDeleteRequestEvent {
     private String washerId;
     private String id;

	public String getWasherId() {
		return washerId;
	}

	public void setWasherId(String washerId) {
		this.washerId = washerId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public AdminGroupMenuDeleteRequestEvent(String washerId, String id) {
		this.washerId = washerId;
		this.id = id;
	}
}
