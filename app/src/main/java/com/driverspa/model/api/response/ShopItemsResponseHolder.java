package com.driverspa.model.api.response;

import com.driverspa.model.ShopItem;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ShopItemsResponseHolder extends BaseResponseHolder {

	@SerializedName("data")
	private Data response;

	public Data getResponse() {
		return response;
	}

	public static class Data {
		ListMetaDataResponse meta;
		List<ShopItem> objects;

		public List<ShopItem> getObjects() {
			return objects;
		}
	}
}
