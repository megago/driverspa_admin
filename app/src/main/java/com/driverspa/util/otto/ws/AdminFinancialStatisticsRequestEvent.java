package com.driverspa.util.otto.ws;


public class AdminFinancialStatisticsRequestEvent {

     private String carwash;
	 private String startDate;

	public AdminFinancialStatisticsRequestEvent(String carwash, String startDate) {
		this.carwash = carwash;
		this.startDate = startDate;
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
}
