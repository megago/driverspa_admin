package com.driverspa.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Staff {

	protected String uid;
	protected String user;
	@SerializedName("first_name")
	protected String firstName;
	@SerializedName("last_name")
	protected String lastName;
	protected String mobile;
	protected String status;
	protected String role;
	@SerializedName("salary_percentage")
	protected Integer salaryPercentage;
	@SerializedName("user_id")
	protected String userId;
	protected Double amount;
	protected String currency;
	protected Date ts;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUser() {
		return userId != null? userId:user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Integer getSalaryPercentage() {
		return salaryPercentage;
	}

	public void setSalaryPercentage(Integer salaryPercentage) {
		this.salaryPercentage = salaryPercentage;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Date getTs() {
		return ts;
	}

	public void setTs(Date ts) {
		this.ts = ts;
	}


	// GSON
	public String serialize() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public static Staff deserialize(String serializedData) {
		Gson gson = new Gson();
		return gson.fromJson(serializedData, Staff.class);
	}
}
