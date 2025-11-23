package com.driverspa.util.otto.ws;


public class AdminCashFinancialRequestEvent {

     private String carwash;
	 private String startDate;
	 private String endDate;

	public AdminCashFinancialRequestEvent(String carwash, String startDate, String endDate) {
		this.carwash = carwash;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public String getCarwash() {
		return carwash;
	}

	public void setCarwash(String carwash) {
		this.carwash = carwash;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
}
