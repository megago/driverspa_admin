package com.driverspa.model.api.request;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import com.driverspa.model.Box;

public class InitialWasherCreateRequest{

	public InitialWasherCreateRequest(){
		
	}
	
	public InitialWasherCreateRequest(String name, ArrayList<Double> lonLat, String city){
		this.name = name;
		this.lonLat = lonLat;
		this.city = city;		
	}
	
	String name;	
	@SerializedName("lonlat")
	ArrayList<Double> lonLat;
	String city;
	@SerializedName("box_settings")
	private ArrayList<Box> boxSettings;

	private String address;


	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<Double> getLonLat() {
		return lonLat;
	}
	public void setLonLat(ArrayList<Double> lonLat) {
		this.lonLat = lonLat;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public ArrayList<Box> getBoxSettings() {
		return boxSettings;
	}
	public void setBoxSettings(ArrayList<Box> boxSettings) {
		this.boxSettings = boxSettings;
	}

	@SerializedName("role_tran")
	RoleTran roleTransactions;

	public static class RoleTran {
		@SerializedName("director_role")
		ArrayList<Transactions> directorRole;
		@SerializedName("operator_role")
		ArrayList<Transactions> operatorRole;

		public ArrayList<Transactions> getDirectorRole() {
			return directorRole;
		}

		public void setDirectorRole(ArrayList<Transactions> directorRole) {
			this.directorRole = directorRole;
		}

		public ArrayList<Transactions> getOperatorRole() {
			return operatorRole;
		}

		public void setOperatorRole(ArrayList<Transactions> operatorRole) {
			this.operatorRole = operatorRole;
		}
	}

	public static class Transactions{
		@SerializedName("tran_cd")
		String transactionCode;
		boolean enabled;

		public Transactions(String transactionCode, boolean enabled) {
			this.transactionCode = transactionCode;
			this.enabled = enabled;
		}

		public String getTransactionCode() {
			return transactionCode;
		}

		public void setTransactionCode(String transactionCode) {
			this.transactionCode = transactionCode;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}

	public RoleTran getRoleTransactions() {
		return roleTransactions;
	}

	public void setRoleTransactions(RoleTran roleTransactions) {
		this.roleTransactions = roleTransactions;
	}
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}



}
