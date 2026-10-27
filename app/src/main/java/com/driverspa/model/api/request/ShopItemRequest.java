package com.driverspa.model.api.request;

import com.google.gson.annotations.SerializedName;

/**
 * Body of POST shop_item/ and PATCH shop_item/<id>/. Null fields are not sent. The initial
 * purchase (initial_stock, unit_cost, payment_type) is only for a new item.
 */
public class ShopItemRequest {

	public String carwash;
	public String name;
	/** An empty string clears the category on PATCH. */
	public String category;
	public Long price;
	@SerializedName("track_stock")
	public Boolean trackStock;
	@SerializedName("low_stock")
	public Integer lowStock;
	@SerializedName("is_active")
	public Boolean isActive;
	@SerializedName("initial_stock")
	public Integer initialStock;
	@SerializedName("unit_cost")
	public Double unitCost;
	@SerializedName("payment_type")
	public String paymentType;
}
