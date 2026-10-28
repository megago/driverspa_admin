package com.driverspa.admin.shift;

import android.os.Handler;
import android.os.Looper;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.api.response.BaseResponseHolder;
import com.driverspa.model.shift.Shift;
import com.driverspa.model.shift.ShiftCurrent;
import com.driverspa.model.shift.ShiftReport;
import com.driverspa.model.shift.ShiftResponses;
import com.driverspa.model.shift.ShiftSettings;
import com.driverspa.util.RetrofitClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * The work_shift/... calls (docs/shift.md). They are made here directly, as PaymentFlow does,
 * instead of through an Assist and Otto events: each screen waits for its own answer. Answers
 * come on the main thread, with the backend's message (localized) as the error.
 */
public final class ShiftApi {

	/** data is null when error is not. */
	public interface Done<T> {
		void done(T data, String error);
	}

	private interface Pick<H, T> {
		T pick(H holder);
	}

	private static final Handler MAIN = new Handler(Looper.getMainLooper());
	private static RetrofitClient.DriverSpaApi api;

	private ShiftApi() {
	}

	private static synchronized RetrofitClient.DriverSpaApi api() {
		if (api == null) api = RetrofitClient.getRestAdapterWithBody();
		return api;
	}

	private static <H extends BaseResponseHolder, T> Callback<H> callback(final Pick<H, T> pick, final Done<T> done) {
		return new Callback<H>() {
			@Override
			public void success(final H data, Response response) {
				MAIN.post(() -> {
					if (data != null && data.getStatus() != null && !"error".equals(data.getStatus())) {
						done.done(pick.pick(data), null);
					} else {
						done.done(null, Shifts.error(data));
					}
				});
			}

			@Override
			public void failure(final RetrofitError error) {
				// An error answer (e.g. 401 "Not allowed") may still carry the envelope.
				BaseResponseHolder body = null;
				try {
					if (error.getResponse() != null) body = (BaseResponseHolder) error.getBodyAs(BaseResponseHolder.class);
				} catch (Exception ignored) {
					body = null;
				}
				final String message = body != null && body.getMessage() != null
						? Shifts.error(body) : BA.str(R.string.connection_error);
				MAIN.post(() -> done.done(null, message));
			}
		};
	}

	public static void current(String washerId, Done<ShiftCurrent> done) {
		api().shiftCurrent(washerId, callback(ShiftResponses.Current::getData, done));
	}

	public static void open(String washerId, double openingCash, List<String> crew, String comment, Done<Shift> done) {
		Map<String, Object> body = new HashMap<>();
		body.put("carwash", washerId);
		body.put("opening_cash", openingCash);
		body.put("crew", crew);
		if (comment != null && !comment.isEmpty()) body.put("comment", comment);
		api().shiftOpen(body, callback(ShiftResponses.One::getData, done));
	}

	public static void history(String washerId, int offset, int limit, boolean shortage, Done<ShiftResponses.HistoryData> done) {
		api().shiftHistory(washerId, offset, limit, shortage ? "1" : null, callback(ShiftResponses.History::getData, done));
	}

	public static void settings(String washerId, Done<ShiftSettings> done) {
		api().shiftSettings(washerId, callback(ShiftResponses.Settings::getData, done));
	}

	/** close_time "" clears it (a null would be left out of the JSON). */
	public static void saveSettings(String washerId, boolean required, int limit, String closeTime, boolean autoClose,
									int autoCloseAfter, Done<ShiftSettings> done) {
		Map<String, Object> body = new HashMap<>();
		body.put("carwash", washerId);
		body.put("required", required);
		body.put("discrepancy_limit", limit);
		body.put("close_time", closeTime != null ? closeTime : "");
		body.put("auto_close", autoClose);
		if (autoClose) body.put("auto_close_after", autoCloseAfter);
		api().shiftSettingsSave(body, callback(ShiftResponses.Settings::getData, done));
	}

	/** The X-report (open) or the Z-report (closed), with the shift in info. */
	public static void report(String shiftId, Done<ShiftReport> done) {
		api().shiftReport(shiftId, callback(ShiftResponses.Report::getData, done));
	}

	/** The blind count: the counted cash goes first, the answer reveals the expected cash. */
	public static void count(String shiftId, double counted, Done<ShiftResponses.CountResult> done) {
		Map<String, Object> body = new HashMap<>();
		body.put("counted_cash", counted);
		api().shiftCount(shiftId, body, callback(ShiftResponses.Count::getData, done));
	}

	/** After a count; for a shift from before the cash drawer cashLeft is null (as before). */
	public static void close(String shiftId, Double cashLeft, String comment, Done<ShiftResponses.ClosedData> done) {
		Map<String, Object> body = new HashMap<>();
		if (cashLeft != null) body.put("cash_left", cashLeft);
		if (comment != null && !comment.isEmpty()) body.put("comment", comment);
		api().shiftClose(shiftId, body, callback(ShiftResponses.Closed::getData, done));
	}

	/** kind: in, out, expense or payout (staffUid); a repeated clientUid is not applied again. */
	public static void cash(String shiftId, String kind, double amount, String reason, String staffUid, String clientUid,
							Done<ShiftResponses.CashResult> done) {
		Map<String, Object> body = new HashMap<>();
		body.put("kind", kind);
		body.put("amount", amount);
		body.put("reason", reason);
		if (staffUid != null) body.put("staff_uid", staffUid);
		body.put("client_uid", clientUid);
		api().shiftCash(shiftId, body, callback(ShiftResponses.Cash::getData, done));
	}

	public static void crew(String shiftId, List<String> crew, Done<Shift> done) {
		Map<String, Object> body = new HashMap<>();
		body.put("crew", crew);
		api().shiftCrew(shiftId, body, callback(ShiftResponses.One::getData, done));
	}

	/** kind note or writeoff (amount: null = what is left of the difference). */
	public static void correction(String shiftId, String kind, String comment, Double amount, Done<Shift> done) {
		Map<String, Object> body = new HashMap<>();
		body.put("kind", kind);
		body.put("comment", comment);
		if (amount != null) body.put("amount", amount);
		api().shiftCorrection(shiftId, body, callback(ShiftResponses.One::getData, done));
	}
}
