package com.driverspa.util.otto.ws;

/** GET shop_sale/?carwash=&day=today|for_date&date=<epoch s> */
public class AdminShopSalesRequestEvent {

	private final String washerId;
	private final String day;
	private final Long date;

	public AdminShopSalesRequestEvent(String washerId, String day, Long date) {
		this.washerId = washerId;
		this.day = day;
		this.date = date;
	}

	public String getWasherId() {
		return washerId;
	}

	public String getDay() {
		return day;
	}

	public Long getDate() {
		return date;
	}

	/** Echoed in the response, so a screen can drop answers to its older requests. */
	private int seq;

	public int getSeq() {
		return seq;
	}

	public AdminShopSalesRequestEvent setSeq(int seq) {
		this.seq = seq;
		return this;
	}
}
