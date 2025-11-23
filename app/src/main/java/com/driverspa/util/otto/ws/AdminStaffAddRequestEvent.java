package com.driverspa.util.otto.ws;


import com.driverspa.model.Staff;

public class AdminStaffAddRequestEvent extends Staff {
    private String washerId;

	public AdminStaffAddRequestEvent(String washerId, String firstName, String lastName, String mobile, String role, String status, Integer salaryPercentage){
		this.washerId = washerId;
		super.firstName = firstName;
		super.lastName = lastName;
		super.role = role;
		super.mobile = mobile;
		super.status = status;
		super.salaryPercentage = salaryPercentage;
	}

	public String getWasherId() {
		return washerId;
	}

	public void setWasherId(String washerId) {
		this.washerId = washerId;
	}
}
