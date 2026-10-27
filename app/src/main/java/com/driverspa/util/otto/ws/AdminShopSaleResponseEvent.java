package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.ShopSaleResponseHolder;

public class AdminShopSaleResponseEvent {

	private final String saleId;
	private final ShopSaleResponseHolder result;

	public AdminShopSaleResponseEvent(String saleId, ShopSaleResponseHolder result) {
		this.saleId = saleId;
		this.result = result;
	}

	public String getSaleId() {
		return saleId;
	}

	public ShopSaleResponseHolder getResult() {
		return result;
	}
}
