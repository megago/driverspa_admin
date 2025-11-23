package com.driverspa.model.api.request;

public class FareRequestBid {
	private String request;
	private String carwash;
	private Double price;

	public FareRequestBid(String request, String carwash, Double price) {
		this.request = request;
		this.carwash = carwash;
		this.price = price;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public String getCarwash() {
		return carwash;
	}

	public void setCarwash(String carwash) {
		this.carwash = carwash;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}
}
