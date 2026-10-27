package com.driverspa.model;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;

/**
 * Created by Yerzhan Tanatov on 23/04/17.
 */
public class CarwashTransaction {

    User user;
    BookInfo booking;
    String description;
    @SerializedName("tran_date")
    Instant tranDate;
    @SerializedName("payment_type")
    String paymentType;
    @SerializedName("operation_type")
    Integer operationType;
    Double amount;
    String currency;
    @SerializedName("client_id")
    String clientId;
    @SerializedName("staff_id")
    String staffId;
    Instant ts;
    @SerializedName("manual_entry")
    Boolean manualEntry;
    /** The shop sale this entry belongs to (ops 15, 17, 18), or null. */
    @SerializedName("shop_sale")
    String shopSale;
    /** The shop item of a purchase or write-off (ops 16, 17), or null. */
    @SerializedName("shop_item")
    String shopItem;

    public String getShopSale() {
        return shopSale;
    }

    public String getShopItem() {
        return shopItem;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BookInfo getBooking() {
        return booking;
    }

    public void setBooking(BookInfo booking) {
        this.booking = booking;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getTranDate() {
        return tranDate;
    }

    public void setTranDate(Instant tranDate) {
        this.tranDate = tranDate;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public Integer getOperationType() {
        return operationType;
    }

    public void setOperationType(Integer operationType) {
        this.operationType = operationType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public Instant getTs() {
        return ts;
    }

    public void setTs(Instant ts) {
        this.ts = ts;
    }

    public Boolean getManualEntry() {
        return manualEntry;
    }

    public void setManualEntry(Boolean manualEntry) {
        this.manualEntry = manualEntry;
    }
}
