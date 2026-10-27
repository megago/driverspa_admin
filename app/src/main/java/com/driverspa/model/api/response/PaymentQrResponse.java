package com.driverspa.model.api.response;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/** The car wash's payment QRs after an upload or delete (and its payment options after an upload). */
public class PaymentQrResponse {

	/** Same shape as the car wash's payment_qr (see Washer#getPaymentQr). */
	@SerializedName("payment_qr")
	private JsonElement paymentQr;

	@SerializedName("payoptions")
	private ArrayList<String> payOptions;

	public JsonElement getPaymentQr() {
		return paymentQr;
	}

	/** Null when the response has none (a delete leaves the options as they are). */
	public ArrayList<String> getPayOptions() {
		return payOptions;
	}
}
