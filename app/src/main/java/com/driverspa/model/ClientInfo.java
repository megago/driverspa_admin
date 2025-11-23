package com.driverspa.model;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class ClientInfo {

	String id;
	String name;
	@SerializedName("mobile")
	String phone;
	@SerializedName("car_no")
	String carNo;
	@SerializedName("car_type")
	Integer carType;
	@SerializedName("car_mark")
	String carMark;
	@SerializedName("avatar")
	String profileImageUrl;
	@SerializedName("finished_books_counts")
	Integer finishedBooksCount;
	@SerializedName("rejected_by_client_books_count")
	Integer rejectedByClientBooksCount;
	@SerializedName("nocome_books_count")
	Integer nocomeBooksCount;
	@SerializedName("last_finished_book_date")
	Date lastFinishedBookDate;
	@SerializedName("is_registered")
	Boolean isRegistered;

	@SerializedName("birth_date")
	Date birthDate;
	@SerializedName("ts")
	Date ts;
    String gender;
	Integer discount;
	@SerializedName("discount_amount")
	Integer discountAmount;
	@SerializedName("bonus_accrual_percentage")
	Integer bonusAccrualPercentage;
	@SerializedName("disable_bonus_accrual")
	boolean disableBonusAccrual;
	@SerializedName("accrued_bonuses")
	Double accruedBonuses;
	@SerializedName("amount")
	Double depositAmount;

	List<CarItem> cars;

	public List<CarItem> getCars() {
		return cars;
	}

	public void setCars(List<CarItem> cars) {
		this.cars = cars;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public Date getTs() {
		return ts;
	}

	public void setTs(Date ts) {
		this.ts = ts;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Integer getDiscount() {
		return discount;
	}

	public void setDiscount(Integer discount) {
		this.discount = discount;
	}

	public Integer getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(Integer discountAmount) {
		this.discountAmount = discountAmount;
	}

	public Integer getBonusAccrualPercentage() {
		return bonusAccrualPercentage;
	}

	public void setBonusAccrualPercentage(Integer bonusAccrualPercentage) {
		this.bonusAccrualPercentage = bonusAccrualPercentage;
	}

	public boolean isDisableBonusAccrual() {
		return disableBonusAccrual;
	}

	public void setDisableBonusAccrual(boolean disableBonusAccrual) {
		this.disableBonusAccrual = disableBonusAccrual;
	}

	public Double getAccruedBonuses() {
		return accruedBonuses;
	}

	public void setAccruedBonuses(Double accruedBonuses) {
		this.accruedBonuses = accruedBonuses;
	}

	public Integer getCarType() {
		return carType;
	}

	public Integer getCarType(String searchedPossibleCarNo) {
		if(cars != null && cars.size() > 0){

			if(!TextUtils.isEmpty(searchedPossibleCarNo))
				for(int i=cars.size()-1;i >= 0;i--){
					if(!cars.get(i).statusDisabled && cars.get(i).getCarNumber().toLowerCase().contains(searchedPossibleCarNo.toLowerCase())){
						carNo = cars.get(i).getCarNumber();
						carType = cars.get(i).getCarType();
						carMark = cars.get(i).getCarModel();
						return cars.get(i).getCarType();
					}
				}

			for(int i=cars.size()-1;i >= 0;i--){
				if(!cars.get(i).statusDisabled){
					carNo = cars.get(i).getCarNumber();
					carType = cars.get(i).getCarType();
					carMark = cars.get(i).getCarModel();
					return cars.get(i).getCarType();
				}
			}

		}
		return carType;
	}

	public void setCarType(Integer carType) {
		this.carType = carType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCarNo() {
		return carNo;
	}

	public String getCarNo(String searchedPossibleCarNo) {
		if(cars != null && cars.size() > 0){

			if(!TextUtils.isEmpty(searchedPossibleCarNo))
				for(int i=cars.size()-1;i >= 0;i--){
					if(!cars.get(i).statusDisabled && cars.get(i).getCarNumber().toLowerCase().contains(searchedPossibleCarNo.toLowerCase())){
						carNo = cars.get(i).getCarNumber();
						carType = cars.get(i).getCarType();
						carMark = cars.get(i).getCarModel();
						return cars.get(i).getCarNumber();
					}
				}

			for(int i=cars.size()-1;i >= 0;i--){
				if(!cars.get(i).statusDisabled){
					carNo = cars.get(i).getCarNumber();
					carType = cars.get(i).getCarType();
					carMark = cars.get(i).getCarModel();
					return cars.get(i).getCarNumber();
				}
			}

		}
		return carNo;
	}


	public void setCarNo(String carNo) {
		this.carNo = carNo;
	}

	public String getCarMark() {
		return carMark;
	}

	public String getCarMark(String searchedPossibleCarNo) {
		if(cars != null && cars.size() > 0){

			if(!TextUtils.isEmpty(searchedPossibleCarNo))
				for(int i=cars.size()-1;i >= 0;i--){
					if(!cars.get(i).statusDisabled && cars.get(i).getCarNumber().toLowerCase().contains(searchedPossibleCarNo.toLowerCase())){
						carNo = cars.get(i).getCarNumber();
						carType = cars.get(i).getCarType();
						carMark = cars.get(i).getCarModel();
						return cars.get(i).getCarModel();
					}
				}

			for(int i=cars.size()-1;i >= 0;i--){
				if(!cars.get(i).statusDisabled){
					carNo = cars.get(i).getCarNumber();
					carType = cars.get(i).getCarType();
					carMark = cars.get(i).getCarModel();
					return cars.get(i).getCarModel();
				}
			}
		}
		return carMark;
	}

	public void setCarMark(String carMark) {
		this.carMark = carMark;
	}

	public String getProfileImageUrl() {
		return profileImageUrl;
	}

	public void setProfileImageUrl(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}

	public Integer getFinishedBooksCount() {
		return finishedBooksCount;
	}

	public void setFinishedBooksCount(Integer finishedBooksCount) {
		this.finishedBooksCount = finishedBooksCount;
	}

	public Integer getRejectedByClientBooksCount() {
		return rejectedByClientBooksCount;
	}

	public void setRejectedByClientBooksCount(Integer rejectedByClientBooksCount) {
		this.rejectedByClientBooksCount = rejectedByClientBooksCount;
	}

	public Integer getNocomeBooksCount() {
		return nocomeBooksCount;
	}

	public void setNocomeBooksCount(Integer nocomeBooksCount) {
		this.nocomeBooksCount = nocomeBooksCount;
	}

	public Date getLastFinishedBookDate() {
		return lastFinishedBookDate;
	}

	public void setLastFinishedBookDate(Date lastFinishedBookDate) {
		this.lastFinishedBookDate = lastFinishedBookDate;
	}

	public Double getDepositAmount() {
		return depositAmount;
	}

	public void setDepositAmount(Double depositAmount) {
		this.depositAmount = depositAmount;
	}

	public Boolean getRegistered() {
		return isRegistered;
	}

	public void setRegistered(Boolean registered) {
		isRegistered = registered;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	// GSON
	public String serialize() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public static ClientInfo deserialize(String serializedData) {
		Gson gson = new Gson();
		return gson.fromJson(serializedData, ClientInfo.class);
	}

}
