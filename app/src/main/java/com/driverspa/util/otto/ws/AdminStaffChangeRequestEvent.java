package com.driverspa.util.otto.ws;


import com.driverspa.model.Staff;

public class AdminStaffChangeRequestEvent extends Staff {
    private String washerId;

	public AdminStaffChangeRequestEvent(String washerId,String uid,String firstName,String lastName,String mobile, String role, String status, Integer salaryPercentage){
		super.uid = uid;
		this.washerId = washerId;
		super.firstName = firstName;
		super.lastName = lastName;
		super.role = role;
		super.status = status;
		super.salaryPercentage = salaryPercentage;
		super.mobile = mobile;
	}


	public String getWasherId() {
		return washerId;
	}

	public void setWasherId(String washerId) {
		this.washerId = washerId;
	}
}
