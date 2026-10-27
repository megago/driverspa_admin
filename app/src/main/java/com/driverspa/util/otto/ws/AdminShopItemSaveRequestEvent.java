package com.driverspa.util.otto.ws;

import com.driverspa.model.api.request.ShopItemRequest;

/** POST shop_item/ when itemId is null, else PATCH shop_item/<id>/. */
public class AdminShopItemSaveRequestEvent {

	private final String itemId;
	private final ShopItemRequest request;

	public AdminShopItemSaveRequestEvent(String itemId, ShopItemRequest request) {
		this.itemId = itemId;
		this.request = request;
	}

	public String getItemId() {
		return itemId;
	}

	public ShopItemRequest getRequest() {
		return request;
	}
}
