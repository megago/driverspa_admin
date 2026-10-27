package com.driverspa.util.otto.ws;

/** GET shop_item/?carwash= */
public class AdminShopItemsRequestEvent {

	private final String washerId;

	public AdminShopItemsRequestEvent(String washerId) {
		this.washerId = washerId;
	}

	public String getWasherId() {
		return washerId;
	}
}
