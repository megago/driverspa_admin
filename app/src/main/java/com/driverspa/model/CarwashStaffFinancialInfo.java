package com.driverspa.model;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;

/**
 * Created by Yerzhan Tanatov on 19/09/17.
 */
public class CarwashStaffFinancialInfo {

    String carwash;
    @SerializedName("staff_id")
    String staffId;

    BooksDataHolder books;

    @SerializedName("start_time")
    Instant startTime;
    @SerializedName("end_time")
    Instant endTime;
    @SerializedName("cars_count")
    Integer carsCount;
    @SerializedName("other_service_count")
    Integer otherServiceCount;
    @SerializedName("cars_amount")
    Double carsAmount;
    @SerializedName("other_service_amount")
    Double otherServiceAmount;
    @SerializedName("total_amount")
    Double totalAmount;
    @SerializedName("staff_salary_accrual")
    Double staffSalaryAccrual;
    @SerializedName("staff_advance_payment")
    Double staffAdvancePayment;
    @SerializedName("staff_total_payment")
    Double staffTotalPayment;


    @SerializedName("manual_accrual")
    Double manualSalaryAccrual;
    @SerializedName("other_services_accrual")
    Double otherServiceAccrual;
    @SerializedName("cars_accrual")
    Double carsAccrual;

    public String getCarwash() {
        return carwash;
    }

    public void setCarwash(String carwash) {
        this.carwash = carwash;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public BooksDataHolder getBooks() {
        return books;
    }

    public void setBooks(BooksDataHolder books) {
        this.books = books;
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

    public Integer getCarsCount() {
        return carsCount;
    }

    public void setCarsCount(Integer carsCount) {
        this.carsCount = carsCount;
    }

    public Integer getOtherServiceCount() {
        return otherServiceCount;
    }

    public void setOtherServiceCount(Integer otherServiceCount) {
        this.otherServiceCount = otherServiceCount;
    }

    public Double getCarsAmount() {
        return carsAmount;
    }

    public void setCarsAmount(Double carsAmount) {
        this.carsAmount = carsAmount;
    }

    public Double getOtherServiceAmount() {
        return otherServiceAmount;
    }

    public void setOtherServiceAmount(Double otherServiceAmount) {
        this.otherServiceAmount = otherServiceAmount;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getStaffSalaryAccrual() {
        return staffSalaryAccrual;
    }

    public void setStaffSalaryAccrual(Double staffSalaryAccrual) {
        this.staffSalaryAccrual = staffSalaryAccrual;
    }

    public Double getStaffAdvancePayment() {
        return staffAdvancePayment;
    }

    public void setStaffAdvancePayment(Double staffAdvancePayment) {
        this.staffAdvancePayment = staffAdvancePayment;
    }

    public Double getStaffTotalPayment() {
        return staffTotalPayment;
    }

    public void setStaffTotalPayment(Double staffTotalPayment) {
        this.staffTotalPayment = staffTotalPayment;
    }

    public Double getManualSalaryAccrual() {
        return manualSalaryAccrual;
    }

    public void setManualSalaryAccrual(Double manualSalaryAccrual) {
        this.manualSalaryAccrual = manualSalaryAccrual;
    }

    public Double getOtherServiceAccrual() {
        return otherServiceAccrual;
    }

    public void setOtherServiceAccrual(Double otherServiceAccrual) {
        this.otherServiceAccrual = otherServiceAccrual;
    }

    public Double getCarsAccrual() {
        return carsAccrual;
    }

    public void setCarsAccrual(Double carsAccrual) {
        this.carsAccrual = carsAccrual;
    }
}
