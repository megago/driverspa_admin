package com.driverspa.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** shop_sale/report: sales per item over a period, the totals and the low stock. */
public class ShopReport {

	List<Entry> items;
	Totals totals;
	@SerializedName("low_stock")
	List<Low> lowStock;

	public List<Entry> getItems() {
		return items;
	}

	public Totals getTotals() {
		return totals;
	}

	public List<Low> getLowStock() {
		return lowStock;
	}

	public static class Entry {
		String item;
		String name;
		Integer qty;
		Double revenue;
		Double cost;
		Double profit;

		public String getName() {
			return name != null ? name : "";
		}

		public int getQty() {
			return qty != null ? qty : 0;
		}

		public double getRevenue() {
			return revenue != null ? revenue : 0;
		}

		/** Null for operators. */
		public Double getCost() {
			return cost;
		}

		/** Null for operators. */
		public Double getProfit() {
			return profit;
		}
	}

	public static class Totals {
		Integer sales;
		Double revenue;
		Double average;
		Double cost;
		Double profit;

		public int getSales() {
			return sales != null ? sales : 0;
		}

		public double getRevenue() {
			return revenue != null ? revenue : 0;
		}

		public double getAverage() {
			return average != null ? average : 0;
		}

		/** Null for operators. */
		public Double getCost() {
			return cost;
		}

		/** Null for operators. */
		public Double getProfit() {
			return profit;
		}
	}

	public static class Low {
		String id;
		String name;
		String category;
		Integer stock;
		@SerializedName("low_stock")
		Integer lowStock;

		public String getName() {
			return name != null ? name : "";
		}

		public String getCategory() {
			return category;
		}

		public int getStock() {
			return stock != null ? stock : 0;
		}

		public int getLowStock() {
			return lowStock != null ? lowStock : 0;
		}
	}
}
