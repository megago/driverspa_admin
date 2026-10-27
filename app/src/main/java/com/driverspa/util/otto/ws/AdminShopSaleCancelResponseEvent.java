package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.ShopSaleResponseHolder;

public class AdminShopSaleCancelResponseEvent {

	private final ShopSaleResponseHolder result;

	public AdminShopSaleCancelResponseEvent(ShopSaleResponseHolder result) {
		this.result = result;
	}

	public ShopSaleResponseHolder getResult() {
		return result;
	}
}
