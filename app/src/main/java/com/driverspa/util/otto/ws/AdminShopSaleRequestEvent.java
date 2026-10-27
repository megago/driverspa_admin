package com.driverspa.util.otto.ws;

/** GET shop_sale/<id>/ */
public class AdminShopSaleRequestEvent {

	private final String saleId;

	public AdminShopSaleRequestEvent(String saleId) {
		this.saleId = saleId;
	}

	public String getSaleId() {
		return saleId;
	}
}
