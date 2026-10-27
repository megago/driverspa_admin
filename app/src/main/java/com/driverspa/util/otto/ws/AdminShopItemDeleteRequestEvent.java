package com.driverspa.util.otto.ws;

/** DELETE shop_item/<id>/ */
public class AdminShopItemDeleteRequestEvent {

	private final String itemId;

	public AdminShopItemDeleteRequestEvent(String itemId) {
		this.itemId = itemId;
	}

	public String getItemId() {
		return itemId;
	}
}
