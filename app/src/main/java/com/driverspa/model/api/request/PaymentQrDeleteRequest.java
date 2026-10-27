package com.driverspa.model.api.request;

/** Body of image_upload/payment_qr/delete: the car wash id and the QR kind ("kaspi_qr"). */
public class PaymentQrDeleteRequest {

	private String carwash;
	private String kind;

	public PaymentQrDeleteRequest(String carwash, String kind) {
		this.carwash = carwash;
		this.kind = kind;
	}

	public String getCarwash() {
		return carwash;
	}

	public String getKind() {
		return kind;
	}
}
