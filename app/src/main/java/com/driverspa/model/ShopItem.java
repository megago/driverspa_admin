package com.driverspa.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;

/**
 * A shop catalog item (shop_item, docs/shop.md). An item that tracks stock has a quantity, a
 * low-stock threshold and an average cost; an untracked one is unlimited. {@code cost} is sent
 * only to managers.
 */
public class ShopItem {

	String id;
	String carwash;
	String name;
	String category;
	Double price;
	@SerializedName("track_stock")
	Boolean trackStock;
	Integer stock;
	@SerializedName("low_stock")
	Integer lowStock;
	@SerializedName("is_low")
	Boolean isLow;
	@SerializedName("is_active")
	Boolean isActive;
	Integer order;
	Double cost;
	Instant ts;

	public String getId() {
		return id;
	}

	public String getCarwash() {
		return carwash;
	}

	public String getName() {
		return name != null ? name : "";
	}

	/** The category, or null when the item has none. */
	public String getCategory() {
		return TextUtils.isEmpty(category) || category.trim().isEmpty() ? null : category.trim();
	}

	public long getPrice() {
		return price != null ? Math.round(price) : 0;
	}

	public boolean isTrackStock() {
		return Boolean.TRUE.equals(trackStock);
	}

	public int getStock() {
		return stock != null ? stock : 0;
	}

	public int getLowStock() {
		return lowStock != null ? lowStock : 0;
	}

	public boolean isLow() {
		if (isLow != null) return isLow;
		return isTrackStock() && getLowStock() > 0 && getStock() <= getLowStock();
	}

	public boolean isActive() {
		return isActive == null || isActive;
	}

	public int getOrder() {
		return order != null ? order : 0;
	}

	/** The average purchase cost; null for operators (not sent). */
	public Double getCost() {
		return cost;
	}

	/** Tracked with nothing left: can't be sold. */
	public boolean isOutOfStock() {
		return isTrackStock() && getStock() <= 0;
	}
}
