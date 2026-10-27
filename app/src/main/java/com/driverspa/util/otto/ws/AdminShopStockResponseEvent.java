package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.ShopItemResponseHolder;

public class AdminShopStockResponseEvent {

	private final boolean purchase;
	private final ShopItemResponseHolder result;

	public AdminShopStockResponseEvent(boolean purchase, ShopItemResponseHolder result) {
		this.purchase = purchase;
		this.result = result;
	}

	public boolean getPurchase() {
		return purchase;
	}

	public ShopItemResponseHolder getResult() {
		return result;
	}
}
