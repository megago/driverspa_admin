package com.driverspa.util.otto.ws;

import com.driverspa.model.api.request.ShopStockRequest;

/** shop_item/<id>/purchase when purchase, else shop_item/<id>/writeoff. */
public class AdminShopStockRequestEvent {

	private final String itemId;
	private final boolean purchase;
	private final ShopStockRequest request;

	public AdminShopStockRequestEvent(String itemId, boolean purchase, ShopStockRequest request) {
		this.itemId = itemId;
		this.purchase = purchase;
		this.request = request;
	}

	public String getItemId() {
		return itemId;
	}

	public boolean getPurchase() {
		return purchase;
	}

	public ShopStockRequest getRequest() {
		return request;
	}
}
