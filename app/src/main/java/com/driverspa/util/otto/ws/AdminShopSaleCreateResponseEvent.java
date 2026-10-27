package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.ShopSaleResponseHolder;

/** The new sale; null result on a connection error. */
public class AdminShopSaleCreateResponseEvent {

	private final ShopSaleResponseHolder result;

	public AdminShopSaleCreateResponseEvent(ShopSaleResponseHolder result) {
		this.result = result;
	}

	public ShopSaleResponseHolder getResult() {
		return result;
	}
}
