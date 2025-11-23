package com.driverspa.util.otto.ws;

import com.driverspa.model.Workshift;

public class AdminWorkshiftAddRequestEvent extends Workshift {
    private String carwash;

	public AdminWorkshiftAddRequestEvent(String carwash) {
		this.carwash = carwash;
	}

	public String getCarwash() {
		return carwash;
	}

	public void setCarwash(String carwash) {
		this.carwash = carwash;
	}
}
