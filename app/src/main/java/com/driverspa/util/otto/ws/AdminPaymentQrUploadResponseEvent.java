package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.PaymentQrResponseHolder;

public class AdminPaymentQrUploadResponseEvent {

	private final PaymentQrResponseHolder data;

	public AdminPaymentQrUploadResponseEvent(PaymentQrResponseHolder data) {
		this.data = data;
	}

	public PaymentQrResponseHolder getResult() {
		return data;
	}
}
