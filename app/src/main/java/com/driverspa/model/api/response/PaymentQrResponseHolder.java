package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

public class PaymentQrResponseHolder extends BaseResponseHolder {

	@SerializedName("data")
	private PaymentQrResponse response;

	public PaymentQrResponse getResponse() {
		return response;
	}
}
