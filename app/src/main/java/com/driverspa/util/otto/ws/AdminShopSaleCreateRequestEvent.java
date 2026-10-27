package com.driverspa.util.otto.ws;

import com.driverspa.model.api.request.ShopSaleRequest;

/** POST shop_sale/ */
public class AdminShopSaleCreateRequestEvent {

	private final ShopSaleRequest request;

	public AdminShopSaleCreateRequestEvent(ShopSaleRequest request) {
		this.request = request;
	}

	public ShopSaleRequest getRequest() {
		return request;
	}
}
