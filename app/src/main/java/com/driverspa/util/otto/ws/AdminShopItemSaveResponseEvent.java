package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.ShopItemResponseHolder;

public class AdminShopItemSaveResponseEvent {

	private final ShopItemResponseHolder result;

	public AdminShopItemSaveResponseEvent(ShopItemResponseHolder result) {
		this.result = result;
	}

	public ShopItemResponseHolder getResult() {
		return result;
	}
}
