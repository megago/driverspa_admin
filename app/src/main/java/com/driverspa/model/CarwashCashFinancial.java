package com.driverspa.model;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;

/**
 * Created by Yerzhan Tanatov on 19/09/17.
 */
public class CarwashCashFinancial {


    String carwash;

    @SerializedName("start_time")
    Instant startTime;
    @SerializedName("end_time")
    Instant endTime;

    @SerializedName("accrued_total_bonusses")
    Double accruedTotalBonusses;
    @SerializedName("bonus_service")
    Double bonusService;
    @SerializedName("card_deposit")
    Double cardDeposit;
    @SerializedName("card_service")
    Double cardService;
    @SerializedName("cash_deposit")
    Double cashDeposit;
    @SerializedName("cash_in")
    Double cashIn;

    @SerializedName("cash_out")
    Double cashOut;
    @SerializedName("cash_service")
    Double cashService;
    @SerializedName("deposit_service")
    Double depositService;

    @SerializedName("other_revenue")
    Double otherRevenue;
    @SerializedName("unearned_revenue")
    Double unearnedRevenue;
    @SerializedName("salary_paid")
    Double salaryPaid;
    @SerializedName("salary_expense")
    Double salaryExpense;
    @SerializedName("service_revenue")
    Double serviceRevenue;

    @SerializedName("total_expense")
    Double totalExpense;
    @SerializedName("total_revenue")
    Double totalRevenue;
    @SerializedName("other_expense")
    Double otherExpense;

    @SerializedName("net_income")
    Double netIncome;

    @SerializedName("transfer_deposit")
    Double transferDeposit;
    @SerializedName("total_cash")
    Double totalCash;

    // The shop (docs/shop.md): goods sales, cost of goods sold, and the shop's cash.
    @SerializedName("goods_revenue")
    Double goodsRevenue;
    @SerializedName("goods_cost")
    Double goodsCost;
    @SerializedName("goods_cash")
    Double goodsCash;
    @SerializedName("goods_card")
    Double goodsCard;
    @SerializedName("goods_purchases")
    Double goodsPurchases;
    @SerializedName("goods_purchases_cash")
    Double goodsPurchasesCash;

    /** Goods sales less canceled ones (ops 15 and 18); 0 when the backend doesn't send it. */
    public double getGoodsRevenue() {
        return goodsRevenue != null ? goodsRevenue : 0;
    }

    /** Cost of goods sold and written off (op 17, less cancellations). */
    public double getGoodsCost() {
        return goodsCost != null ? goodsCost : 0;
    }

    /** Cash taken by the shop less canceled sales. */
    public double getGoodsCash() {
        return goodsCash != null ? goodsCash : 0;
    }

    /** Card and Kaspi QR shop sales (not part of the cash). */
    public double getGoodsCard() {
        return goodsCard != null ? goodsCard : 0;
    }

    /** All goods purchases (op 16). */
    public double getGoodsPurchases() {
        return goodsPurchases != null ? goodsPurchases : 0;
    }

    /** Goods purchases paid in cash. */
    public double getGoodsPurchasesCash() {
        return goodsPurchasesCash != null ? goodsPurchasesCash : 0;
    }

    public String getCarwash() {
        return carwash;
    }

    public void setCarwash(String carwash) {
        this.carwash = carwash;
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

    public Double getAccruedTotalBonusses() {
        return accruedTotalBonusses;
    }

    public void setAccruedTotalBonusses(Double accruedTotalBonusses) {
        this.accruedTotalBonusses = accruedTotalBonusses;
    }

    public Double getBonusService() {
        return bonusService;
    }

    public void setBonusService(Double bonusService) {
        this.bonusService = bonusService;
    }

    public Double getCardDeposit() {
        return cardDeposit;
    }

    public void setCardDeposit(Double cardDeposit) {
        this.cardDeposit = cardDeposit;
    }

    public Double getCardService() {
        return cardService;
    }

    public void setCardService(Double cardService) {
        this.cardService = cardService;
    }

    public Double getCashDeposit() {
        return cashDeposit;
    }

    public void setCashDeposit(Double cashDeposit) {
        this.cashDeposit = cashDeposit;
    }

    public Double getCashIn() {
        return cashIn;
    }

    public void setCashIn(Double cashIn) {
        this.cashIn = cashIn;
    }

    public Double getCashOut() {
        return cashOut;
    }

    public void setCashOut(Double cashOut) {
        this.cashOut = cashOut;
    }

    public Double getCashService() {
        return cashService;
    }

    public void setCashService(Double cashService) {
        this.cashService = cashService;
    }

    public Double getDepositService() {
        return depositService;
    }

    public void setDepositService(Double depositService) {
        this.depositService = depositService;
    }

    public Double getOtherRevenue() {
        return otherRevenue;
    }

    public void setOtherRevenue(Double otherRevenue) {
        this.otherRevenue = otherRevenue;
    }

    public Double getSalaryExpense() {
        return salaryExpense;
    }

    public void setSalaryExpense(Double salaryExpense) {
        this.salaryExpense = salaryExpense;
    }

    public Double getServiceRevenue() {
        return serviceRevenue;
    }

    public void setServiceRevenue(Double serviceRevenue) {
        this.serviceRevenue = serviceRevenue;
    }

    public Double getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(Double totalExpense) {
        this.totalExpense = totalExpense;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Double getOtherExpense() {
        return otherExpense;
    }

    public void setOtherExpense(Double otherExpense) {
        this.otherExpense = otherExpense;
    }

    public Double getTransferDeposit() {
        return transferDeposit;
    }

    public void setTransferDeposit(Double transferDeposit) {
        this.transferDeposit = transferDeposit;
    }

    public Double getNetIncome() {
        return netIncome;
    }

    public void setNetIncome(Double netIncome) {
        this.netIncome = netIncome;
    }

    public Double getSalaryPaid() {
        return salaryPaid;
    }

    public void setSalaryPaid(Double salaryPaid) {
        this.salaryPaid = salaryPaid;
    }

    public Double getTotalCash() {
        return totalCash;
    }

    public void setTotalCash(Double totalCash) {
        this.totalCash = totalCash;
    }

    public Double getUnearnedRevenue() {
        return unearnedRevenue;
    }

    public void setUnearnedRevenue(Double unearnedRevenue) {
        this.unearnedRevenue = unearnedRevenue;
    }
}
