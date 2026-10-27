package com.driverspa.model;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;
import java.util.List;

/**
 * A shop sale (shop_sale, docs/shop.md): paid in full when made, so it has lines and the
 * payment split. {@code cost} and {@code profit} are sent only to managers.
 */
public class ShopSale {

	public static final String PAID = "paid";
	public static final String CANCELED = "canceled";

	String id;
	String carwash;
	Integer number;
	String day;
	String user;
	@SerializedName("user_name")
	String userName;
	List<Line> lines;
	Double total;
	@SerializedName("price_details")
	List<BookInfo.PriceDetail> priceDetails;
	String status;
	@SerializedName("client_uid")
	String clientUid;
	Instant ts;
	@SerializedName("canceled_ts")
	Instant canceledTs;
	@SerializedName("canceled_by")
	String canceledBy;
	@SerializedName("canceled_by_name")
	String canceledByName;
	@SerializedName("cancel_reason")
	String cancelReason;
	Double cost;
	Double profit;

	public String getId() {
		return id;
	}

	public int getNumber() {
		return number != null ? number : 0;
	}

	public String getDay() {
		return day;
	}

	public String getUserName() {
		return userName;
	}

	public List<Line> getLines() {
		return lines;
	}

	public double getTotal() {
		return total != null ? total : 0;
	}

	public List<BookInfo.PriceDetail> getPriceDetails() {
		return priceDetails;
	}

	public String getStatus() {
		return status;
	}

	public boolean isCanceled() {
		return CANCELED.equals(status);
	}

	public boolean isPaid() {
		return PAID.equals(status);
	}

	public Instant getTs() {
		return ts;
	}

	public Instant getCanceledTs() {
		return canceledTs;
	}

	public String getCanceledByName() {
		return canceledByName;
	}

	public String getCancelReason() {
		return cancelReason;
	}

	/** Null for operators. */
	public Double getCost() {
		return cost;
	}

	/** Null for operators. */
	public Double getProfit() {
		return profit;
	}

	public static class Line {
		String item;
		String name;
		Double price;
		Integer qty;
		Double sum;
		Boolean tracked;
		Double cost;

		public String getItem() {
			return item;
		}

		public String getName() {
			return name != null ? name : "";
		}

		public double getPrice() {
			return price != null ? price : 0;
		}

		public int getQty() {
			return qty != null ? qty : 0;
		}

		public double getSum() {
			return sum != null ? sum : getPrice() * getQty();
		}

		/** The unit cost at the sale; null for operators. */
		public Double getCost() {
			return cost;
		}
	}
}
