package com.driverspa.model.api.request;

/** Body of shop_sale/<id>/cancel. */
public class ShopSaleCancelRequest {

	public String reason;

	public ShopSaleCancelRequest(String reason) {
		this.reason = reason;
	}
}
