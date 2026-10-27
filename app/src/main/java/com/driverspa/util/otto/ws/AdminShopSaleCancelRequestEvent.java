package com.driverspa.util.otto.ws;

/** POST shop_sale/<id>/cancel */
public class AdminShopSaleCancelRequestEvent {

	private final String saleId;
	private final String reason;

	public AdminShopSaleCancelRequestEvent(String saleId, String reason) {
		this.saleId = saleId;
		this.reason = reason;
	}

	public String getSaleId() {
		return saleId;
	}

	public String getReason() {
		return reason;
	}
}
