package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.PaymentQrResponseHolder;

public class AdminPaymentQrDeleteResponseEvent {

	private final PaymentQrResponseHolder data;

	public AdminPaymentQrDeleteResponseEvent(PaymentQrResponseHolder data) {
		this.data = data;
	}

	public PaymentQrResponseHolder getResult() {
		return data;
	}
}
