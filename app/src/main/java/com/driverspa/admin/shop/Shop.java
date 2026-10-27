package com.driverspa.admin.shop;

import android.text.TextUtils;
import android.widget.EditText;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.BookInfo;
import com.driverspa.model.Staff;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.model.api.response.BaseResponseHolder;
import com.driverspa.util.Constants;
import com.driverspa.util.Functions;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Shared helpers of the shop screens (docs/shop.md): money, roles, limits, server messages. */
public final class Shop {

	/** The backend's limits: quantities, then prices and amounts, then other integers. */
	public static final long MAX_QTY = 100_000;
	public static final long MAX_MONEY = 1_000_000_000L;
	public static final long MAX_OTHER = 1_000_000;

	/** Payment types of a purchase (restock): cash, card, bank transfer. */
	public static final String PAY_TRANSFER = "T";

	private static final int[] TILE_COLORS = {
			0xFF3E6DDA, 0xFF0F9D8A, 0xFFB5651D, 0xFF8E44AD, 0xFFC0392B,
			0xFF2E86C1, 0xFF16A085, 0xFFD35400, 0xFF6C5CE7, 0xFF7F8C2D,
	};

	private Shop() {
	}

	/** Whole tenge with thousands separators, as elsewhere in the app. */
	public static String money(double value) {
		return new DecimalFormat("#,###.##").format(value) + " ₸";
	}

	/**
	 * A manager manages the catalog, cancels sales and sees costs: the owner, or a director who
	 * works (not on leave). Everyone else (operators, directors on leave, users not in the staff
	 * list) only sells and looks at the sales. The backend checks it again.
	 */
	public static boolean isManager(Washer washer, User user) {
		if (user == null) return false;
		if (user.isOwner()) return true;
		if (washer == null || user.getId() == null) return false;
		Staff staff = Functions.getWasherStaff(washer, user.getId());
		return staff != null && Constants.DIRECTOR.equals(staff.getRole()) && Constants.WORKS.equals(staff.getStatus());
	}

	/** A stable colour for an item's initial. */
	public static int colorFor(String name) {
		int h = name == null ? 0 : name.trim().toLowerCase().hashCode();
		return TILE_COLORS[Math.abs(h % TILE_COLORS.length)];
	}

	public static String initial(String name) {
		if (name == null) return "?";
		String s = name.trim();
		if (s.isEmpty()) return "?";
		return s.substring(0, s.offsetByCodePoints(0, 1)).toUpperCase();
	}

	/** A field's whole number; -1 when empty or not a number. */
	public static long number(EditText field) {
		String s = field.getText().toString().trim().replace(" ", "").replace(" ", "");
		if (s.isEmpty()) return -1;
		try {
			return Long.parseLong(s);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	/** A field's decimal number (a cost may have kopecks); -1 when empty or not a number. */
	public static double decimal(EditText field) {
		String s = field.getText().toString().trim().replace(" ", "").replace(',', '.');
		if (s.isEmpty()) return -1;
		try {
			return Double.parseDouble(s);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	/** The label of a payment type (C, CD, QR, T). */
	public static String paymentName(String type) {
		if (Constants.PAYMENT_CASH.equals(type)) return BA.str(R.string.cash);
		if (Constants.PAYMENT_CARD.equals(type)) return BA.str(R.string.card);
		if (Constants.PAYMENT_KASPI_QR.equals(type)) return BA.str(R.string.kaspi_qr);
		if (PAY_TRANSFER.equals(type)) return BA.str(R.string.transfer);
		return type != null ? type : "";
	}

	/** "Наличные" for one part, "Наличные 500 + Карта 1 000" for a split. */
	public static String paymentSummary(List<BookInfo.PriceDetail> parts) {
		if (parts == null || parts.isEmpty()) return "";
		if (parts.size() == 1) return paymentName(parts.get(0).getPaymentType());
		StringBuilder b = new StringBuilder();
		for (BookInfo.PriceDetail p : parts) {
			if (b.length() > 0) b.append(" + ");
			b.append(paymentName(p.getPaymentType())).append(' ')
					.append(money(p.getAmount() != null ? p.getAmount() : 0));
		}
		return b.toString();
	}

	/** "dd.MM.yyyy HH:mm" in the car wash's zone. */
	public static String dateTime(Instant ts) {
		HashMap<String, String> t = Functions.formatDateTime(ts, Functions.getCarwashZone());
		String date = t.get(Functions.DATE);
		String time = t.get(Functions.TIME);
		if (TextUtils.isEmpty(date)) return "";
		return date + " " + time;
	}

	public static String time(Instant ts) {
		HashMap<String, String> t = Functions.formatDateTime(ts, Functions.getCarwashZone());
		return t.get(Functions.TIME) != null ? t.get(Functions.TIME) : "";
	}

	/** The message of a failed response, localized. */
	public static String error(BaseResponseHolder result) {
		if (result == null) return BA.str(R.string.connection_error);
		return message(result.getMessage());
	}

	private static final Pattern NOT_ENOUGH = Pattern.compile("^Not enough stock: (.*) \\((-?\\d+) left\\)$");
	private static final Pattern HIDDEN = Pattern.compile("^Item is hidden: (.*)$");
	private static final Pattern VALUE = Pattern.compile("^Invalid value: (.*)$");

	/** The backend's English shop messages (docs/shop.md, "Errors") in the app's language. */
	public static String message(String message) {
		if (TextUtils.isEmpty(message)) return BA.str(R.string.error_word);
		String m = message.trim();
		Matcher matcher = NOT_ENOUGH.matcher(m);
		if (matcher.matches()) return BA.str(R.string.shop_err_not_enough, matcher.group(1), matcher.group(2));
		matcher = HIDDEN.matcher(m);
		if (matcher.matches()) return BA.str(R.string.shop_err_item_hidden, matcher.group(1));
		matcher = VALUE.matcher(m);
		if (matcher.matches()) return BA.str(R.string.shop_err_value, matcher.group(1));
		switch (m) {
			case "Name is not specified": return BA.str(R.string.shop_err_name);
			case "Name is too long (60 characters at most)": return BA.str(R.string.shop_err_name_long);
			case "An item with this name already exists": return BA.str(R.string.shop_err_name_exists);
			case "Category is too long (40 characters at most)": return BA.str(R.string.shop_err_category_long);
			case "Invalid price": return BA.str(R.string.shop_err_price);
			case "Invalid low stock threshold": return BA.str(R.string.shop_err_low_stock);
			case "Invalid quantity": return BA.str(R.string.shop_err_qty);
			case "Invalid unit cost": return BA.str(R.string.shop_err_unit_cost);
			case "Unknown payment type": return BA.str(R.string.shop_err_payment_type);
			case "Invalid payment amount": return BA.str(R.string.shop_err_payment_amount);
			case "Write off the stock first": return BA.str(R.string.shop_err_write_off_first);
			case "Item has history, hide it instead": return BA.str(R.string.shop_err_has_history);
			case "Item does not track stock": return BA.str(R.string.shop_err_not_tracked);
			case "Reason is not specified": return BA.str(R.string.shop_err_reason);
			case "Comment is too long (300 characters at most)": return BA.str(R.string.shop_err_comment_long);
			case "Reason is too long (300 characters at most)": return BA.str(R.string.shop_err_reason_long);
			case "No items in the sale": return BA.str(R.string.shop_err_no_lines);
			case "Unknown item": return BA.str(R.string.shop_err_unknown_item);
			case "Payment total does not match the sale total": return BA.str(R.string.shop_err_total_mismatch);
			case "Only a paid sale can be canceled": return BA.str(R.string.shop_err_only_paid);
			case "The item is busy, try again": return BA.str(R.string.shop_err_busy);
			case "Shop operations can't be entered manually": return BA.str(R.string.shop_err_manual);
			case "Not allowed": return BA.str(R.string.no_access_rights);
			default: return m;
		}
	}
}
