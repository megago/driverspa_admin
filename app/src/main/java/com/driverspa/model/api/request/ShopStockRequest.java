package com.driverspa.model.api.request;

import com.google.gson.annotations.SerializedName;

/** Body of shop_item/<id>/purchase ({qty, unit_cost, payment_type, comment?}) and writeoff ({qty, comment}). */
public class ShopStockRequest {

	public Integer qty;
	@SerializedName("unit_cost")
	public Double unitCost;
	@SerializedName("payment_type")
	public String paymentType;
	public String comment;
	/** One key per attempt: a repeated key isn't applied again. */
	@SerializedName("client_uid")
	public String clientUid;
}
