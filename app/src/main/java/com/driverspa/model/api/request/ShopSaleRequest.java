package com.driverspa.model.api.request;

import com.driverspa.model.BookInfo;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/** Body of POST shop_sale/. A repeated client_uid returns the first sale instead of selling again. */
public class ShopSaleRequest {

	public String carwash;
	@SerializedName("client_uid")
	public String clientUid;
	public List<Line> lines;
	@SerializedName("price_details")
	public List<BookInfo.PriceDetail> priceDetails;

	public static class Line {
		public String item;
		public Integer qty;

		public Line(String item, int qty) {
			this.item = item;
			this.qty = qty;
		}
	}
}
