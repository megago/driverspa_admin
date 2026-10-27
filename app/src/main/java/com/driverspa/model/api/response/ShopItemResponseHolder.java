package com.driverspa.model.api.response;

import com.driverspa.model.ShopItem;
import com.google.gson.annotations.SerializedName;

public class ShopItemResponseHolder extends BaseResponseHolder {

	@SerializedName("data")
	private ShopItem response;

	public ShopItem getResponse() {
		return response;
	}
}
