package com.driverspa.model;

import com.driverspa.model.api.response.BaseResponseHolder;
import com.google.gson.annotations.SerializedName;

/**
 * corporate_client/find: the company whose car list has a plate (client null when none), and its
 * contract price for the car type (null: the order's normal price). docs/corporate-clients.md.
 */
public class CorporateFind extends BaseResponseHolder {
	@SerializedName("data")
	Data data;

	public Data getData() {
		return data;
	}

	public static class Data {
		@SerializedName("client")
		Client client;
		@SerializedName("price")
		Double price;

		public Client getClient() {
			return client;
		}

		public Double getPrice() {
			return price;
		}
	}

	public static class Client {
		@SerializedName("id")
		String id;
		@SerializedName("name")
		String name;

		public String getName() {
			return name;
		}
	}
}
