package com.driverspa.model.api.response;

import com.driverspa.model.ShopSale;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ShopSalesResponseHolder extends BaseResponseHolder {

	@SerializedName("data")
	private Data response;

	public Data getResponse() {
		return response;
	}

	public static class Data {
		Meta meta;
		List<ShopSale> objects;

		public List<ShopSale> getObjects() {
			return objects;
		}

		/** The day's paid total (meta.revenue). */
		public double getRevenue() {
			return meta != null && meta.revenue != null ? meta.revenue : 0;
		}
	}

	public static class Meta extends ListMetaDataResponse {
		Double revenue;
	}
}
