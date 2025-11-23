package com.driverspa.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Yerzhan Tanatov on 22/04/17.
 */
public class CarwashAccountDetail {
    @SerializedName("account_code")
    Integer accountCode;
    String carwash;
    Double amount;
    @SerializedName("amount_opening")
    Double openingAmount;
    @SerializedName("amount_ending")
    Double endingAmount;
    String currency;


    public Integer getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(Integer accountCode) {
        this.accountCode = accountCode;
    }

    public String getCarwash() {
        return carwash;
    }

    public void setCarwash(String carwash) {
        this.carwash = carwash;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getOpeningAmount() {
        return openingAmount;
    }

    public void setOpeningAmount(Double openingAmount) {
        this.openingAmount = openingAmount;
    }

    public Double getEndingAmount() {
        return endingAmount;
    }

    public void setEndingAmount(Double endingAmount) {
        this.endingAmount = endingAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
