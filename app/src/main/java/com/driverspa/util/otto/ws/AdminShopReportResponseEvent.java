package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.ShopReportResponseHolder;

public class AdminShopReportResponseEvent {

	private final ShopReportResponseHolder result;

	public AdminShopReportResponseEvent(ShopReportResponseHolder result) {
		this.result = result;
	}

	public ShopReportResponseHolder getResult() {
		return result;
	}

	/** Echoed in the response, so a screen can drop answers to its older requests. */
	private int seq;

	public int getSeq() {
		return seq;
	}

	public AdminShopReportResponseEvent setSeq(int seq) {
		this.seq = seq;
		return this;
	}
}
