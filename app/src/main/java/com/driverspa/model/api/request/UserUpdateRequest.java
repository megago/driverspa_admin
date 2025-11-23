package com.driverspa.model.api.request;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;

import java.util.List;

import com.driverspa.model.CarItem;

public class UserUpdateRequest{

	@SerializedName("first_name")
    private String firstName;

	private List<CarItem> cars;

	@SerializedName("car_number")
	private String carNumber;

	@DatabaseField
	@SerializedName("car_model")	
    private String carModel;
	
	@SerializedName("last_name")			
    private String lastName;

	public List<CarItem> getCars() {
		return cars;
	}

	public void setCars(List<CarItem> cars) {
		this.cars = cars;
	}

	public String getCarNumber() {
		return carNumber;
	}

	public void setCarNumber(String carNumber) {
		this.carNumber = carNumber;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getCarModel() {
		return carModel;
	}

	public void setCarModel(String carModel) {
		this.carModel = carModel;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

}
