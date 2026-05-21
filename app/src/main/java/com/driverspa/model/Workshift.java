package com.driverspa.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.driverspa.util.Converters;

import java.time.Instant;

/**
 * Created by Yerzhan Tanatov on 20/04/17.
 */
public class Workshift {

    String id;
    @SerializedName("user")
    User openedUser;
    @SerializedName("closed_user")
    User closedUser;
    Integer duration;
    @SerializedName("start_time")
    Instant startTime;
    @SerializedName("end_time")
    Instant endTime;
    Instant ts;
    @SerializedName("opening_balance")
    Double openingBalance;
    @SerializedName("closing_balance")
    Double closingBalance;
    String currency;
    @SerializedName("financial_statistics")
    CarwashFinancialStatistics financialStatistics;

    public User getOpenedUser() {
        return openedUser;
    }

    public void setOpenedUser(User openedUser) {
        this.openedUser = openedUser;
    }

    public User getClosedUser() {
        return closedUser;
    }

    public void setClosedUser(User closedUser) {
        this.closedUser = closedUser;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public Instant getTs() {
        return ts;
    }

    public void setTs(Instant ts) {
        this.ts = ts;
    }

    public Double getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(Double openingBalance) {
        this.openingBalance = openingBalance;
    }

    public Double getClosingBalance() {
        return closingBalance;
    }

    public void setClosingBalance(Double closingBalance) {
        this.closingBalance = closingBalance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CarwashFinancialStatistics getFinancialStatistics() {
        return financialStatistics;
    }

    public void setFinancialStatistics(CarwashFinancialStatistics financialStatistics) {
        this.financialStatistics = financialStatistics;
    }

    // GSON
    public String serialize() {
        Gson gson = Converters.gsonWithDate();
        return gson.toJson(this);
    }

    public static Workshift deserialize(String serializedData) {
        Gson gson = Converters.gsonWithDate();
        return gson.fromJson(serializedData, Workshift.class);
    }
}
