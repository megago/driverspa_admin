package com.driverspa.util.otto.ws;

public class WasherTimeTableBookRequestEvent {
     private String washerId;
     private String day;
	 private Long date;

	 public WasherTimeTableBookRequestEvent(String washerId, String day, Long date){
    	 this.washerId = washerId;
    	 this.day = day;
		 this.date = date;
     }
	
	public String getDay(){
		return day;
	}
	
    public String getWasherId() {
		return washerId;
	}

	public void setWasherId(String washerId) {
		this.washerId = washerId;
	}

	public Long getDate() {
		return date;
	}

	public void setDate(Long date) {
		this.date = date;
	}
}
