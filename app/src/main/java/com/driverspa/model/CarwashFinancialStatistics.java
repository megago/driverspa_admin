package com.driverspa.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * Created by Yerzhan Tanatov on 22/04/17.
 */
public class CarwashFinancialStatistics {

    List<CarwashAccountDetail> accounts;
    String carwash;
    Date startTime;
    Date endTime;

    Double expense;
    @SerializedName("expense_opening")
    Double expenseOpening;
    @SerializedName("expense_ending")
    Double expenseEnding;
    Double revenue;
    @SerializedName("revenue_opening")
    Double revenueOpening;
    @SerializedName("revenue_ending")
    Double revenueEnding;
    @SerializedName("net_profit")
    Double netProfit;


    public List<CarwashAccountDetail> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<CarwashAccountDetail> accounts) {
        this.accounts = accounts;
    }

    public String getCarwash() {
        return carwash;
    }

    public void setCarwash(String carwash) {
        this.carwash = carwash;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Double getExpense() {
        return expense;
    }

    public void setExpense(Double expense) {
        this.expense = expense;
    }

    public Double getExpenseOpening() {
        return expenseOpening;
    }

    public void setExpenseOpening(Double expenseOpening) {
        this.expenseOpening = expenseOpening;
    }

    public Double getExpenseEnding() {
        return expenseEnding;
    }

    public void setExpenseEnding(Double expenseEnding) {
        this.expenseEnding = expenseEnding;
    }

    public Double getRevenue() {
        return revenue;
    }

    public void setRevenue(Double revenue) {
        this.revenue = revenue;
    }

    public Double getRevenueOpening() {
        return revenueOpening;
    }

    public void setRevenueOpening(Double revenueOpening) {
        this.revenueOpening = revenueOpening;
    }

    public Double getRevenueEnding() {
        return revenueEnding;
    }

    public void setRevenueEnding(Double revenueEnding) {
        this.revenueEnding = revenueEnding;
    }

    public Double getNetProfit() {
        return netProfit;
    }

    public void setNetProfit(Double netProfit) {
        this.netProfit = netProfit;
    }
}
