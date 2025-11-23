package com.driverspa.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;

/**
 * Created by Yerzhan Tanatov on 28/10/15.
 */
public class CarItem {

    @DatabaseField
    @SerializedName("car_type")
    Integer carType;
    @DatabaseField(id = true)
    @SerializedName("car_number")
    String  carNumber;
    @DatabaseField
    @SerializedName("car_model")
    String  carModel;
    @SerializedName("status_disabled")
    boolean statusDisabled;

    public Integer getCarType() {
        return carType;
    }

    public void setCarType(Integer carType) {
        this.carType = carType;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }
}
