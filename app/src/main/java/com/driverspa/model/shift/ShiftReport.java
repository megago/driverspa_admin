package com.driverspa.model.shift;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * The X-report (open shift) or the Z-report (closed, frozen at the close), as
 * work_shift/&lt;id&gt;/report returns it (docs/shift.md, "Report"). For an operator on an open
 * shift the expected cash and the cash flows are null.
 */
public class ShiftReport {

	/** Payment ways in the order they are shown: cash, card, Kaspi QR, deposit, bonuses. */
	public static final String[] PAYMENT_KEYS = {"C", "CD", "QR", "DA", "B"};
	/** The cash flows in the order they are shown. */
	public static final String[] FLOW_KEYS = {"washes", "shop", "deposits", "other_income", "cash_in",
			"cash_out", "expenses", "payouts", "purchases", "refunds", "other"};

	String shift;
	Integer number;
	@SerializedName("final")
	Boolean isFinal;
	Instant from;
	Instant to;
	Revenue revenue;
	Washes washes;
	ShopPart shop;
	Integer cancellations;
	@SerializedName("no_shows")
	Integer noShows;
	Refunds refunds;
	Expenses expenses;
	Cash cash;
	@SerializedName("cash_moves")
	List<CashMove> cashMoves;
	List<WasherPay> washers;
	List<BoxStat> boxes;
	@SerializedName("open_orders")
	OpenOrders openOrders;
	@SerializedName("close_kind")
	String closeKind;
	String comment;
	/** GET work_shift/&lt;id&gt;/report only: the shift itself and the car wash's name. */
	Shift info;
	@SerializedName("carwash_name")
	String carwashName;

	public String getShift() {
		return shift;
	}

	public Integer getNumber() {
		return number;
	}

	/** A Z-report: the shift is closed. */
	public boolean isFinal() {
		return Boolean.TRUE.equals(isFinal);
	}

	public Instant getFrom() {
		return from;
	}

	public Instant getTo() {
		return to;
	}

	public Revenue getRevenue() {
		return revenue != null ? revenue : new Revenue();
	}

	public Washes getWashes() {
		return washes != null ? washes : new Washes();
	}

	public ShopPart getShop() {
		return shop != null ? shop : new ShopPart();
	}

	public int getCancellations() {
		return cancellations != null ? cancellations : 0;
	}

	public int getNoShows() {
		return noShows != null ? noShows : 0;
	}

	public Refunds getRefunds() {
		return refunds != null ? refunds : new Refunds();
	}

	public Expenses getExpenses() {
		return expenses != null ? expenses : new Expenses();
	}

	public Cash getCash() {
		return cash != null ? cash : new Cash();
	}

	public List<CashMove> getCashMoves() {
		return cashMoves;
	}

	public List<WasherPay> getWashers() {
		return washers;
	}

	public List<BoxStat> getBoxes() {
		return boxes;
	}

	public OpenOrders getOpenOrders() {
		return openOrders;
	}

	public String getCloseKind() {
		return closeKind;
	}

	public String getComment() {
		return comment;
	}

	public Shift getInfo() {
		return info;
	}

	public String getCarwashName() {
		return carwashName;
	}

	static double value(Double d) {
		return d != null ? d : 0;
	}

	static double value(Map<String, Double> map, String key) {
		if (map == null) return 0;
		Double d = map.get(key);
		return d != null ? d : 0;
	}

	public static class Revenue {
		Double total;
		Double washes;
		Double shop;
		@SerializedName("by_payment")
		Map<String, Double> byPayment;

		public double getTotal() {
			return value(total);
		}

		public double getWashes() {
			return value(washes);
		}

		public double getShop() {
			return value(shop);
		}

		public double byPayment(String key) {
			return value(byPayment, key);
		}
	}

	public static class Washes {
		Integer orders;
		Integer cars;
		@SerializedName("other_services")
		Integer otherServices;
		Double revenue;
		Double average;
		Double discounts;

		public int getOrders() {
			return orders != null ? orders : 0;
		}

		public int getCars() {
			return cars != null ? cars : 0;
		}

		public int getOtherServices() {
			return otherServices != null ? otherServices : 0;
		}

		public double getRevenue() {
			return value(revenue);
		}

		public double getAverage() {
			return value(average);
		}

		public double getDiscounts() {
			return value(discounts);
		}
	}

	public static class ShopPart {
		Integer sales;
		Integer canceled;
		Double revenue;
		Double refunds;

		public int getSales() {
			return sales != null ? sales : 0;
		}

		public int getCanceled() {
			return canceled != null ? canceled : 0;
		}

		public double getRevenue() {
			return value(revenue);
		}
	}

	public static class Refunds {
		Double total;

		public double getTotal() {
			return value(total);
		}
	}

	public static class Expenses {
		Double total;
		Double cash;
		List<Expense> items;

		public double getTotal() {
			return value(total);
		}

		public List<Expense> getItems() {
			return items;
		}
	}

	public static class Expense {
		String id;
		Double amount;
		Boolean cash;
		String description;
		@SerializedName("user_name")
		String userName;
		Instant ts;

		public double getAmount() {
			return value(amount);
		}

		public boolean isCash() {
			return Boolean.TRUE.equals(cash);
		}

		public String getDescription() {
			return description;
		}

		public Instant getTs() {
			return ts;
		}
	}

	/** The drawer. The last four are filled in a Z-report only. */
	public static class Cash {
		Double opening;
		/** Null for an operator on an open shift. */
		Map<String, Double> flows;
		Double expected;
		Double counted;
		Double difference;
		Double left;
		Double removed;

		public Double getOpening() {
			return opening;
		}

		public Map<String, Double> getFlows() {
			return flows;
		}

		public Double getExpected() {
			return expected;
		}

		public Double getCounted() {
			return counted;
		}

		public Double getDifference() {
			return difference;
		}

		public Double getLeft() {
			return left;
		}

		public Double getRemoved() {
			return removed;
		}
	}

	public static class CashMove {
		public static final String IN = "in";
		public static final String OUT = "out";
		public static final String EXPENSE = "expense";
		public static final String PAYOUT = "payout";

		String id;
		String kind;
		Double amount;
		String reason;
		@SerializedName("staff_uid")
		String staffUid;
		@SerializedName("staff_name")
		String staffName;
		@SerializedName("user_name")
		String userName;
		Instant ts;

		public String getKind() {
			return kind;
		}

		public double getAmount() {
			return value(amount);
		}

		/** Money out of the drawer is negative. */
		public double getSignedAmount() {
			return IN.equals(kind) ? getAmount() : -getAmount();
		}

		public String getReason() {
			return reason;
		}

		public String getStaffName() {
			return staffName;
		}

		public Instant getTs() {
			return ts;
		}
	}

	public static class WasherPay {
		@SerializedName("staff_uid")
		String staffUid;
		String name;
		@SerializedName("on_shift")
		Boolean onShift;
		Integer cars;
		Double earned;
		Double paid;

		public String getStaffUid() {
			return staffUid;
		}

		public String getName() {
			return name;
		}

		public boolean isOnShift() {
			return Boolean.TRUE.equals(onShift);
		}

		public int getCars() {
			return cars != null ? cars : 0;
		}

		public double getEarned() {
			return value(earned);
		}

		public double getPaid() {
			return value(paid);
		}
	}

	public static class BoxStat {
		@SerializedName("box_id")
		String boxId;
		String name;
		Integer cars;
		@SerializedName("avg_minutes")
		Double avgMinutes;

		public String getName() {
			return name != null ? name : boxId;
		}

		public int getCars() {
			return cars != null ? cars : 0;
		}

		public Double getAvgMinutes() {
			return avgMinutes;
		}
	}

	/** Orders to look at before closing: cars still in a box, and finished orders not paid. */
	public static class OpenOrders {
		@SerializedName("in_box")
		List<Order> inBox;
		List<Order> unpaid;

		public List<Order> getInBox() {
			return inBox;
		}

		public List<Order> getUnpaid() {
			return unpaid;
		}

		public boolean isEmpty() {
			return (inBox == null || inBox.isEmpty()) && (unpaid == null || unpaid.isEmpty());
		}
	}

	public static class Order {
		String id;
		@SerializedName("client_key")
		String clientKey;
		@SerializedName("box_name")
		String boxName;
		String status;
		Double price;
		Instant time;

		public String getId() {
			return id;
		}

		public String getClientKey() {
			return clientKey;
		}

		public String getBoxName() {
			return boxName;
		}

		public double getPrice() {
			return value(price);
		}

		public Instant getTime() {
			return time;
		}
	}
}
