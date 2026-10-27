package com.driverspa.util.otto.ws;

/** GET shop_sale/report?carwash=&from=<epoch s>&to=<epoch s> */
public class AdminShopReportRequestEvent {

	private final String washerId;
	private final long from;
	private final long to;

	public AdminShopReportRequestEvent(String washerId, long from, long to) {
		this.washerId = washerId;
		this.from = from;
		this.to = to;
	}

	public String getWasherId() {
		return washerId;
	}

	public long getFrom() {
		return from;
	}

	public long getTo() {
		return to;
	}

	/** Echoed in the response, so a screen can drop answers to its older requests. */
	private int seq;

	public int getSeq() {
		return seq;
	}

	public AdminShopReportRequestEvent setSeq(int seq) {
		this.seq = seq;
		return this;
	}
}
