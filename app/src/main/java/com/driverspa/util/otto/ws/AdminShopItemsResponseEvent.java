package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.ShopItemsResponseHolder;

/** The catalog; null result on a connection error. */
public class AdminShopItemsResponseEvent {

	private final ShopItemsResponseHolder result;

	public AdminShopItemsResponseEvent(ShopItemsResponseHolder result) {
		this.result = result;
	}

	public ShopItemsResponseHolder getResult() {
		return result;
	}
}
