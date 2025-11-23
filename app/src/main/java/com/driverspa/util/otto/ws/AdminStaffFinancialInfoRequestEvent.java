package com.driverspa.util.otto.ws;


public class AdminStaffFinancialInfoRequestEvent {

     private String carwash;
	 private String staffId;
	 private String startDate;
	 private String endDate;

	public AdminStaffFinancialInfoRequestEvent(String carwash, String staffId, String startDate, String endDate) {
		this.carwash = carwash;
		this.staffId = staffId;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public String getCarwash() {
		return carwash;
	}

	public void setCarwash(String carwash) {
		this.carwash = carwash;
	}

	public String getStaffId() {
		return staffId;
	}

	public void setStaffId(String staffId) {
		this.staffId = staffId;
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
