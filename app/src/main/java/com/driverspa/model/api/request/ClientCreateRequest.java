package com.driverspa.model.api.request;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.driverspa.util.Converters;

import java.time.Instant;
import java.util.List;

import com.driverspa.model.CarItem;

public class ClientCreateRequest {
	
	String id;
	String name;
	@SerializedName("mobile")
	String phone;
	@SerializedName("birth_date")
	Instant birthDate;
    String gender;
	Integer discount;
	@SerializedName("bonus_accrual_percentage")
	Integer bonusAccrualPercentage;
	@SerializedName("disable_bonus_accrual")
	boolean disableBonusAccrual;
	List<CarItem> cars;
	String carwash;

	private ClientCreateRequest(Builder builder){
		   this.id = builder.id;
		   this.name = builder.name;
		   this.phone = builder.phone;
		   this.birthDate = builder.birthDate;
		   this.disableBonusAccrual = builder.disableBonusAccrual;
		   this.discount = builder.discount;
		   this.bonusAccrualPercentage = builder.bonusAccrualPercentage;
		   this.cars = builder.cars;
		   this.carwash = builder.carwash;
	}

	public static class Builder{
		String name;
		String phone;
		Instant birthDate;
		String gender;
		Integer discount;
		Integer discountAmount;
		Integer bonusAccrualPercentage;
		boolean disableBonusAccrual;
		Double accruedBonuses;
		List<CarItem> cars;
		String carwash;
		String id;
		
		
		public Builder id(String id) {
			this.id = id;
			return this;
		}
		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder phone(String phone) {
			this.phone = phone;
			return this;
		}

		public Builder birthDate(Instant birthDate) {
			this.birthDate = birthDate;
			return this;
		}

		public Builder gender(String gender) {
			this.gender = gender;
			return this;
		}

		public Builder discount(Integer discount) {
			this.discount = discount;
			return this;
		}


		public Builder bonusAccrualPercentage(Integer bonusAccrualPercentage) {
			this.bonusAccrualPercentage = bonusAccrualPercentage;
			return this;
		}

		public Builder disableBonusAccrual(boolean disableBonusAccrual) {
			this.disableBonusAccrual = disableBonusAccrual;
			return this;
		}


		public Builder cars(List<CarItem> cars) {
			this.cars = cars;
			return this;
		}
		public Builder carwash(String carwash) {
			this.carwash = carwash;
			return this;
		}

		public ClientCreateRequest create(){
			return new ClientCreateRequest(this);
		}
	}


	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPhone() {
		return phone;
	}

	public Instant getBirthDate() {
		return birthDate;
	}

	public String getGender() {
		return gender;
	}

	public Integer getDiscount() {
		return discount;
	}

	public Integer getBonusAccrualPercentage() {
		return bonusAccrualPercentage;
	}

	public boolean isDisableBonusAccrual() {
		return disableBonusAccrual;
	}

	public List<CarItem> getCars() {
		return cars;
	}

	public String getCarwash() {
		return carwash;
	}

	// GSON
	public String serialize() {
		Gson gson = Converters.gsonWithDate();
		return gson.toJson(this);
	}

	public static ClientCreateRequest deserialize(String serializedData) {
		Gson gson = Converters.gsonWithDate();
		return gson.fromJson(serializedData, ClientCreateRequest.class);
	}

}
