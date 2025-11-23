package com.driverspa.util.otto.ws;


import java.util.List;

import com.driverspa.model.ReversePrices;

public class AdminGroupMenuChangeRequestEvent {
     private String washerId;
     private String id;
	  String name;
	 private List<Integer> services;
	 private List<ReversePrices> prices;

	public AdminGroupMenuChangeRequestEvent(String washerId, String id, String name, List<Integer> services, List<ReversePrices> prices) {
		this.washerId = washerId;
		this.id = id;
		this.name = name;
		this.services = services;
		this.prices = prices;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWasherId() {
		return washerId;
	}

	public void setWasherId(String washerId) {
		this.washerId = washerId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Integer> getServices() {
		return services;
	}

	public void setServices(List<Integer> services) {
		this.services = services;
	}

	public List<ReversePrices> getPrices() {
		return prices;
	}

	public void setPrices(List<ReversePrices> prices) {
		this.prices = prices;
	}
}
