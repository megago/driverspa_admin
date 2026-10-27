package com.driverspa.util.otto.ws;

public class AdminPaymentQrDeleteRequestEvent {

	private final String washerId;

	public AdminPaymentQrDeleteRequestEvent(String washerId) {
		this.washerId = washerId;
	}

	public String getWasherId() {
		return washerId;
	}
}
