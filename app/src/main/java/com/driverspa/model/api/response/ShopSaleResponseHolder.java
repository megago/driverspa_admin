package com.driverspa.model.api.response;

import com.driverspa.model.ShopSale;
import com.google.gson.annotations.SerializedName;

public class ShopSaleResponseHolder extends BaseResponseHolder {

	@SerializedName("data")
	private ShopSale response;

	public ShopSale getResponse() {
		return response;
	}
}
