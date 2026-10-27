package com.driverspa.model.api.response;

import com.driverspa.model.ShopStockMove;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ShopMovesResponseHolder extends BaseResponseHolder {

	@SerializedName("data")
	private Data response;

	public Data getResponse() {
		return response;
	}

	public static class Data {
		ListMetaDataResponse meta;
		List<ShopStockMove> objects;

		public List<ShopStockMove> getObjects() {
			return objects;
		}

		/** All the item's moves, for paging; -1 when not sent. */
		public long getTotalCount() {
			return meta != null ? meta.getTotalCount() : -1;
		}
	}
}
