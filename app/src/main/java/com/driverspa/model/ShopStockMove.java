package com.driverspa.model;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;

/** One entry of an item's stock history (shop_item/<id>/moves). */
public class ShopStockMove {

	public static final String PURCHASE = "purchase";
	public static final String SALE = "sale";
	public static final String WRITEOFF = "writeoff";
	public static final String CANCEL = "cancel";

	String id;
	String item;
	String kind;
	Integer qty;
	@SerializedName("payment_type")
	String paymentType;
	String sale;
	@SerializedName("sale_number")
	Integer saleNumber;
	String user;
	@SerializedName("user_name")
	String userName;
	String comment;
	Instant ts;
	@SerializedName("unit_cost")
	Double unitCost;

	public String getKind() {
		return kind;
	}

	/** Signed: positive for purchases and cancellations, negative for sales and write-offs. */
	public int getQty() {
		return qty != null ? qty : 0;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public String getSale() {
		return sale;
	}

	public Integer getSaleNumber() {
		return saleNumber;
	}

	public String getUserName() {
		return userName;
	}

	public String getComment() {
		return comment;
	}

	public Instant getTs() {
		return ts;
	}

	/** Null for operators. */
	public Double getUnitCost() {
		return unitCost;
	}
}
