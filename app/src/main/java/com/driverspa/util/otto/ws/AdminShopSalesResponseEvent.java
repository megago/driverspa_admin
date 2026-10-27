package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.ShopSalesResponseHolder;

public class AdminShopSalesResponseEvent {

	private final ShopSalesResponseHolder result;

	public AdminShopSalesResponseEvent(ShopSalesResponseHolder result) {
		this.result = result;
	}

	public ShopSalesResponseHolder getResult() {
		return result;
	}

	/** Echoed in the response, so a screen can drop answers to its older requests. */
	private int seq;

	public int getSeq() {
		return seq;
	}

	public AdminShopSalesResponseEvent setSeq(int seq) {
		this.seq = seq;
		return this;
	}
}
