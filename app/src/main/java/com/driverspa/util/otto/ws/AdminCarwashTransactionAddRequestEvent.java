package com.driverspa.util.otto.ws;

import com.driverspa.model.CarwashTransaction;

public class AdminCarwashTransactionAddRequestEvent extends CarwashTransaction {
    private String washerId;
    private String carwash;


	public String getWasherId() {
		return washerId;
	}

	public void setWasherId(String washerId) {
		this.washerId = washerId;
	}

	public String getCarwash() {
		return carwash;
	}

	public void setCarwash(String carwash) {
		this.carwash = carwash;
	}
}
