package com.driverspa.admin.payment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.assist.BaseAssist;
import com.driverspa.model.BookInfo;
import com.driverspa.model.Washer;
import com.driverspa.model.api.request.BookingRequest;
import com.driverspa.model.api.response.BookInfoResponseHolder;
import com.driverspa.model.api.response.BookResponseHolder;
import com.driverspa.model.api.response.WasherResponseHolder;
import com.driverspa.util.Constants;
import com.driverspa.util.RetrofitClient;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.ws.AdminBookStopResponseEvent;
import com.driverspa.util.otto.ws.AdminFinishQueueResponseEvent;
import com.driverspa.util.otto.ws.AdminMarkAsClientPaidResponseEvent;
import com.driverspa.util.otto.ws.ApiErrorEvent;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Payment is taken when an order is finished, not when it is created:
 * "Завершить" loads the order and, when no payment is recorded on it, opens the payment window,
 * saves the payment (client_paid with price_details) and then finishes the order (finish_queue
 * for queue entries, finish for bookings).
 *
 * The calls are made here directly instead of through the Assist classes: several screens are
 * registered on the bus at once, and each of them would react to the intermediate responses.
 * At the end the usual response events (AdminFinishQueueResponseEvent, AdminBookStopResponseEvent,
 * AdminMarkAsClientPaidResponseEvent) are posted, so the screens reload and toast as before.
 */
public final class PaymentFlow {

	/** Called once on the main thread when the flow ends, whatever the outcome. */
	public interface OnDone {
		void onDone();
	}

	private static final int MODE_FINISH = 0;
	private static final int MODE_PAY = 1;
	private static final int MODE_SALE = 2;

	private static final Handler MAIN = new Handler(Looper.getMainLooper());
	private static RetrofitClient.DriverSpaApi api;

	private final Activity activity;
	private final String bookingId;
	private final Washer washer;
	private final int mode;
	private final int confirmQuestion;
	private final OnDone onDone;

	private ProgressDialog progress;
	private PaymentDialog paymentDialog;
	private int pending;
	private BookInfo book;
	private String loadError;
	private boolean carwashLoaded;
	private String qrUrl;
	private boolean done;

	private PaymentFlow(Activity activity, String bookingId, Washer washer, int mode, int confirmQuestion, OnDone onDone) {
		this.activity = activity;
		this.bookingId = bookingId;
		this.washer = washer;
		this.mode = mode;
		this.confirmQuestion = confirmQuestion;
		this.onDone = onDone;
	}

	/**
	 * "Завершить": pays first when no payment is recorded, then finishes.
	 *
	 * @param confirmQuestion string asked before finishing an order that needs no payment, as the
	 *                        screen did before (0: finish without asking)
	 */
	public static void finish(Activity activity, BookInfo book, Washer washer, int confirmQuestion, OnDone onDone) {
		new PaymentFlow(activity, book.getId(), washer, MODE_FINISH, confirmQuestion, onDone).start();
	}

	/** "Оплата" on an old order that was finished without payment: pays only. */
	public static void pay(Activity activity, BookInfo book, Washer washer, OnDone onDone) {
		new PaymentFlow(activity, book.getId(), washer, MODE_PAY, 0, onDone).start();
	}

	/** A shop sale right after it was created: the payment window, then finish_queue. */
	public static void payNewSale(Activity activity, String bookingId, Washer washer, OnDone onDone) {
		new PaymentFlow(activity, bookingId, washer, MODE_SALE, 0, onDone).start();
	}

	/** The order has no recorded payment: unpaid, or "paid" (the default) with no split. */
	public static boolean needsPayment(BookInfo book) {
		if (!Boolean.TRUE.equals(book.getPaid())) return true;
		return price(book) > 0 && (book.getPriceDetails() == null || book.getPriceDetails().isEmpty());
	}

	/** An old order that was finished without payment (finishing now requires one). */
	public static boolean isFinishedUnpaid(BookInfo book) {
		String status = book.getStatus();
		return !Boolean.TRUE.equals(book.getPaid())
				&& (Constants.FINISHED.equals(status) || Constants.QUEUED_FINISHED.equals(status));
	}

	/** The order's price (an int on the backend), 0 when missing. */
	public static double price(BookInfo book) {
		try {
			return Double.parseDouble(book.getPrice());
		} catch (Exception e) {
			return 0;
		}
	}

	private static synchronized RetrofitClient.DriverSpaApi api() {
		if (api == null) api = RetrofitClient.getRestAdapterWithBody();
		return api;
	}

	// ---- loading ----

	private void start() {
		showProgress();
		pending = washer != null ? 2 : 1;
		api().companyBookingInfo(bookingId, new Callback<BookInfoResponseHolder>() {
			@Override
			public void success(final BookInfoResponseHolder data, Response response) {
				MAIN.post(() -> {
					if (data != null && BaseAssist.isSuccess(data) && data.getResponse() != null) book = data.getResponse();
					else loadError = data != null ? data.getMessage() : null;
					loaded();
				});
			}

			@Override
			public void failure(final RetrofitError error) {
				MAIN.post(() -> {
					BA.getEventBus().post(new ApiErrorEvent(error));
					loadError = BA.str(R.string.connection_error);
					loaded();
				});
			}
		});
		if (washer == null) return;
		// The QR may have been uploaded or removed on another device: read it fresh.
		api().adminWasherInfo(washer.getId(), new Callback<WasherResponseHolder>() {
			@Override
			public void success(final WasherResponseHolder data, Response response) {
				MAIN.post(() -> {
					if (data != null && BaseAssist.isSuccess(data) && data.getResponse() != null
							&& data.getResponse().getWasher() != null) {
						carwashLoaded = true;
						qrUrl = data.getResponse().getWasher().getKaspiQrUrl();
					}
					loaded();
				});
			}

			@Override
			public void failure(RetrofitError error) {
				MAIN.post(PaymentFlow.this::loaded);
			}
		});
	}

	private void loaded() {
		if (--pending > 0) return;
		hideProgress();
		if (!alive()) return;
		if (book == null) {
			ToastUtil.display(activity, TextUtils.isEmpty(loadError) ? BA.str(R.string.err_get_data) : loadError);
			finishFlow();
			return;
		}
		if (!carwashLoaded && washer != null) qrUrl = washer.getKaspiQrUrl();
		if (mode == MODE_FINISH && !needsPayment(book)) {
			confirmAndFinish();
			return;
		}
		openPaymentWindow();
	}

	// ---- an order that needs no payment: finish as before ----

	private void confirmAndFinish() {
		if (confirmQuestion == 0) {
			showProgress();
			finishOrder(null);
			return;
		}
		AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(activity);
		dialog.setTitle(BA.str(confirmQuestion));
		dialog.setNeutralButton(BA.str(R.string.no_word), (d, which) -> finishFlow());
		dialog.setPositiveButton(BA.str(R.string.yes_word), (d, which) -> {
			showProgress();
			finishOrder(null);
		});
		dialog.setOnCancelListener(d -> finishFlow());
		dialog.show();
	}

	// ---- the payment window ----

	private void openPaymentWindow() {
		paymentDialog = new PaymentDialog(activity, book, qrUrl, mode != MODE_PAY, new PaymentDialog.Listener() {
			@Override
			public void onSubmit(List<BookInfo.PriceDetail> parts) {
				savePayment(parts);
			}

			@Override
			public void onCancel() {
				finishFlow();
			}
		});
		paymentDialog.show();
	}

	private void savePayment(List<BookInfo.PriceDetail> parts) {
		paymentDialog.setBusy(true);
		BookingRequest request = new BookingRequest(book.getId());
		request.setPaid(true);
		request.setPriceDetails(parts);
		api().markAsClientPaidRequest(request, new Callback<BookResponseHolder>() {
			@Override
			public void success(final BookResponseHolder data, Response response) {
				MAIN.post(() -> paymentSaved(data, null));
			}

			@Override
			public void failure(final RetrofitError error) {
				MAIN.post(() -> paymentSaved(null, error));
			}
		});
	}

	private void paymentSaved(BookResponseHolder data, RetrofitError error) {
		if (!alive()) return;
		if (data == null || !BaseAssist.isSuccess(data)) {
			// The window stays open with the reason, so the payment can be corrected.
			paymentDialog.setBusy(false);
			paymentDialog.showError(data != null ? data.getMessage() : BA.str(R.string.connection_error));
			return;
		}
		if (mode == MODE_PAY) {
			paymentDialog.dismiss();
			ToastUtil.display(activity, BA.str(R.string.payment_saved));
			BA.getEventBus().post(new AdminMarkAsClientPaidResponseEvent(data));
			finishFlow();
			return;
		}
		finishOrder(data);
	}

	// ---- finishing ----

	/** @param paid the client_paid response when the payment was just saved, else null */
	private void finishOrder(final BookResponseHolder paid) {
		Callback<BookResponseHolder> callback = new Callback<BookResponseHolder>() {
			@Override
			public void success(final BookResponseHolder data, Response response) {
				MAIN.post(() -> finished(paid, data, null));
			}

			@Override
			public void failure(final RetrofitError error) {
				MAIN.post(() -> finished(paid, null, error));
			}
		};
		if (book.isQueued()) api().finishQueueRequest(new BookingRequest(book.getId()), callback);
		else api().adminBookingStop(new BookingRequest(book.getId()), callback);
	}

	private void finished(BookResponseHolder paid, BookResponseHolder data, RetrofitError error) {
		hideProgress();
		if (paymentDialog != null) paymentDialog.dismiss();
		if (error != null) BA.getEventBus().post(new ApiErrorEvent(error));
		boolean ok = data != null && BaseAssist.isSuccess(data);
		// Paid but not finished (e.g. no washer chosen): the screens reload to show it paid, and
		// the finish error is shown. The next "Завершить" then only asks for confirmation.
		if (!ok && paid != null) BA.getEventBus().post(new AdminMarkAsClientPaidResponseEvent(paid));
		// A sale is paid from its own form, where no list is listening for the events below.
		if (mode == MODE_SALE && alive()) {
			ToastUtil.display(activity, ok ? BA.str(R.string.service_provided)
					: (data != null && !TextUtils.isEmpty(data.getMessage()) ? data.getMessage() : BA.str(R.string.error_word)));
		}
		if (book.isQueued()) BA.getEventBus().post(new AdminFinishQueueResponseEvent(data));
		else BA.getEventBus().post(new AdminBookStopResponseEvent(data));
		finishFlow();
	}

	// ---- helpers ----

	private boolean alive() {
		return !activity.isFinishing() && !activity.isDestroyed();
	}

	private void showProgress() {
		if (!alive()) return;
		if (progress == null) {
			progress = new ProgressDialog(activity);
			progress.setTitle("");
			progress.setIndeterminate(true);
			progress.setCancelable(false);
			progress.setMessage(BA.str(R.string.loading_dots));
		}
		progress.show();
	}

	private void hideProgress() {
		if (progress != null && progress.isShowing() && alive()) progress.dismiss();
	}

	private void finishFlow() {
		if (done) return;
		done = true;
		if (onDone != null) onDone.onDone();
	}
}
