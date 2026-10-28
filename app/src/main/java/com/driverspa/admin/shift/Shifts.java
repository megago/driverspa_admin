package com.driverspa.admin.shift;

import android.text.TextUtils;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.shop.Shop;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.model.api.response.BaseResponseHolder;
import com.driverspa.model.shift.Shift;
import com.driverspa.util.Constants;
import com.driverspa.util.Functions;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Shared helpers of the shift screens (docs/shift.md): money, roles, the backend's messages. */
public final class Shifts {

	private Shifts() {
	}

	public static String money(double value) {
		return Shop.money(value);
	}

	public static String money(Double value) {
		return value == null ? "—" : Shop.money(value);
	}

	/** "−500 ₸", "+200 ₸", "0 ₸". */
	public static String signed(double value) {
		long tiyn = Math.round(value * 100);
		if (tiyn == 0) return Shop.money(0);
		return (tiyn < 0 ? "−" : "+") + Shop.money(Math.abs(value));
	}

	/** The colour of a difference: red for a shortage, green for a surplus, light for none. */
	public static int toneColor(double difference) {
		long tiyn = Math.round(difference * 100);
		if (tiyn < 0) return R.color.OrangeRed;
		if (tiyn > 0) return R.color.LimeGreen;
		return R.color.BlueLight;
	}

	/** "недостача", "излишек" or "без расхождений". */
	public static String toneText(double difference) {
		long tiyn = Math.round(difference * 100);
		if (tiyn < 0) return BA.str(R.string.shift_shortage);
		if (tiyn > 0) return BA.str(R.string.shift_surplus);
		return BA.str(R.string.shift_no_difference);
	}

	/** Whether a difference is above the limit (compared in tiyn, as the backend does). */
	public static boolean aboveLimit(double difference, int limit) {
		return Math.round(Math.abs(difference) * 100) > Math.round(limit * 100.0);
	}

	/** "3 ч 05 мин". */
	public static String duration(int minutes) {
		int m = Math.max(0, minutes);
		return BA.str(R.string.shift_duration_value, m / 60, String.format(java.util.Locale.US, "%02d", m % 60));
	}

	public static String dateTime(Instant ts) {
		return ts == null ? "" : Shop.dateTime(ts);
	}

	public static String time(Instant ts) {
		return ts == null ? "" : Shop.time(ts);
	}

	/**
	 * A cash amount typed by the user: digits with one decimal separator, at most two decimals;
	 * 0 is allowed (an empty drawer). Null when it is not an amount.
	 */
	public static Double parseCash(String text) {
		if (text == null) return null;
		String s = text.replace(" ", "").replace(" ", "").replace(" ", "").replace(',', '.').trim();
		if (!s.matches("^\\d{1,9}(\\.\\d{1,2})?$")) return null;
		try {
			return Double.parseDouble(s);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** A plain number for an input field: "3000", "2500.5". */
	public static String plain(double value) {
		if (value == Math.rint(value)) return String.valueOf((long) value);
		return String.valueOf(Math.round(value * 100) / 100.0);
	}

	/** The owner or a working director: settings and corrections (the backend checks it again). */
	public static boolean isManager(Washer washer, User user) {
		return Shop.isManager(washer, user);
	}

	/** Staff the panel refuses (washers, on leave, dismissed): no shift calls for them. */
	public static boolean mayUse(Washer washer, User user) {
		if (washer == null || user == null) return false;
		if (user.isOwner()) return true;
		String role = Functions.getUserRole(washer, user.getId());
		String status = Functions.getUserStatus(washer, user.getId());
		return (Constants.DIRECTOR.equals(role) || Constants.OPERATOR.equals(role)) && Constants.WORKS.equals(status);
	}

	/** A key for one cash operation: a retry after a lost answer is not applied twice. */
	public static String newClientUid() {
		return UUID.randomUUID().toString();
	}

	/** "Смена №12", or "Смена (старая)" for one from before the cash drawer (no number). */
	public static String title(Shift s) {
		if (s == null || s.getNumber() == null) return BA.str(R.string.shift_old);
		return BA.str(R.string.shift_no, s.getNumberText());
	}

	public static String closeKindLabel(String kind) {
		if (Shift.KIND_AUTO.equals(kind)) return BA.str(R.string.shift_kind_auto);
		if (Shift.KIND_LEGACY.equals(kind)) return BA.str(R.string.shift_kind_legacy);
		return BA.str(R.string.shift_kind_operator);
	}

	public static String cashKindLabel(String kind) {
		if ("out".equals(kind)) return BA.str(R.string.shift_cash_out);
		if ("expense".equals(kind)) return BA.str(R.string.shift_cash_expense);
		if ("payout".equals(kind)) return BA.str(R.string.shift_cash_payout);
		return BA.str(R.string.shift_cash_in);
	}

	/** The message of a failed answer, in the app's language. */
	public static String error(BaseResponseHolder result) {
		if (result == null) return BA.str(R.string.connection_error);
		String message = result.getMessage();
		if (TextUtils.isEmpty(message)) return BA.str(R.string.error_word);
		return Shop.message(message);
	}

	private static final Pattern UNKNOWN_WASHER = Pattern.compile("^Unknown washer: (.*)$");

	/**
	 * The backend's English shift messages (docs/shift.md, "Errors") in the app's language, or
	 * null when it is not one of them. BaseResponseHolder.getMessage() applies it, so every
	 * screen that shows a server message (payment, finish, shop sale, finance) shows these too.
	 */
	public static String localize(String message) {
		if (message == null) return null;
		String m = message.trim();
		Matcher matcher = UNKNOWN_WASHER.matcher(m);
		if (matcher.matches()) return BA.str(R.string.shift_err_washer);
		switch (m) {
			case "Open a shift first": return BA.str(R.string.shift_err_required);
			case "Invalid cash amount":
			case "Invalid amount": return BA.str(R.string.shift_err_amount);
			case "Comment is required for the difference": return BA.str(R.string.shift_err_comment);
			case "Comment is not specified": return BA.str(R.string.shift_err_comment_empty);
			case "Count the cash first": return BA.str(R.string.shift_err_count_first);
			case "Cash moved since the count, count again": return BA.str(R.string.shift_err_moved);
			case "Cash left is more than counted": return BA.str(R.string.shift_err_left);
			case "The shift is closed":
			case "The shift is already closed, refresh the list":
			case "This workshift has been already closed": return BA.str(R.string.shift_err_closed);
			case "The shift is open": return BA.str(R.string.shift_err_open);
			case "This shift has no cash drawer: close it and open a new one": return BA.str(R.string.shift_err_legacy);
			case "You cannot open a shift, close the open one first":
			case "You can not open new workshift, there is already not closed": return BA.str(R.string.shift_err_already_open);
			case "Staff member not found": return BA.str(R.string.shift_err_staff);
			case "Nothing to write off": return BA.str(R.string.shift_err_nothing);
			case "The shift was changed, try again": return BA.str(R.string.shift_err_changed);
			case "Set the closing time first": return BA.str(R.string.shift_err_close_time);
			default: return null;
		}
	}
}
