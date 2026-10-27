package com.driverspa.admin.shop;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.BookInfo;
import com.driverspa.util.Constants;
import com.driverspa.util.HttpClient;
import com.squareup.picasso.Callback;

import java.util.ArrayList;
import java.util.List;

/**
 * The payment window of a shop sale: Наличные (with an optional "Получено" and the change),
 * Карта, Kaspi QR (only when the car wash has one uploaded) and Смешанная. A sale is paid in
 * full; there is no deposit or bonus. The window stays open on an error.
 */
public class ShopPaymentDialog {

	public interface Listener {
		void onSubmit(List<BookInfo.PriceDetail> parts);

		void onCancel();
	}

	private static final int TAB_CASH = 0;
	private static final int TAB_CARD = 1;
	private static final int TAB_QR = 2;
	private static final int TAB_MIXED = 3;

	private final Dialog dialog;
	private final Listener listener;
	private final long due;
	private final String qrUrl;
	private int tab = TAB_CASH;
	private boolean busy;
	private boolean qrRequested;

	private final View[] tabs;
	private final View cashLayout, qrLayout, qrProgress, mixedLayout, mixedQrRow, progress;
	private final TextView cashChange, qrHint, mixedRemaining, error;
	private final ImageView qrImage;
	private final EditText cashReceived, mixedCash, mixedCard, mixedQr;
	private final Button cancel, confirm;

	/**
	 * @param subtitle what is sold, in short
	 * @param qrUrl    the car wash's Kaspi QR image, or null when none is uploaded
	 */
	public ShopPaymentDialog(Activity activity, long due, String subtitle, String qrUrl, Listener listener) {
		this.listener = listener;
		this.due = due;
		this.qrUrl = qrUrl;

		View root = LayoutInflater.from(activity).inflate(R.layout.dialog_shop_payment, null);
		dialog = new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(root);
		Window window = dialog.getWindow();
		if (window != null) {
			window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		dialog.setCanceledOnTouchOutside(false);
		dialog.setOnCancelListener(d -> this.listener.onCancel());

		((TextView) root.findViewById(R.id.paymentSubtitle)).setText(subtitle);
		((TextView) root.findViewById(R.id.paymentAmount)).setText(Shop.money(due));
		tabs = new View[]{root.findViewById(R.id.tabCash), root.findViewById(R.id.tabCard),
				root.findViewById(R.id.tabQr), root.findViewById(R.id.tabMixed)};
		cashLayout = root.findViewById(R.id.cashLayout);
		cashReceived = root.findViewById(R.id.cashReceived);
		cashChange = root.findViewById(R.id.cashChange);
		qrLayout = root.findViewById(R.id.qrLayout);
		qrImage = root.findViewById(R.id.qrImage);
		qrProgress = root.findViewById(R.id.qrProgress);
		qrHint = root.findViewById(R.id.qrHint);
		mixedLayout = root.findViewById(R.id.mixedLayout);
		mixedCash = root.findViewById(R.id.mixedCash);
		mixedCard = root.findViewById(R.id.mixedCard);
		mixedQr = root.findViewById(R.id.mixedQr);
		mixedQrRow = root.findViewById(R.id.mixedQrRow);
		mixedRemaining = root.findViewById(R.id.mixedRemaining);
		error = root.findViewById(R.id.paymentError);
		progress = root.findViewById(R.id.paymentProgress);
		cancel = root.findViewById(R.id.paymentCancel);
		confirm = root.findViewById(R.id.paymentConfirm);

		boolean hasQr = qrUrl != null;
		tabs[TAB_QR].setVisibility(hasQr ? View.VISIBLE : View.GONE);
		mixedQrRow.setVisibility(hasQr ? View.VISIBLE : View.GONE);

		for (int i = 0; i < tabs.length; i++) {
			final int index = i;
			tabs[i].setOnClickListener(v -> selectTab(index));
		}
		TextWatcher watcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void afterTextChanged(Editable s) {
				updateAmounts();
			}
		};
		for (EditText field : new EditText[]{cashReceived, mixedCash, mixedCard, mixedQr}) {
			field.addTextChangedListener(watcher);
		}
		cancel.setOnClickListener(v -> dialog.cancel());
		confirm.setOnClickListener(v -> submit());
		selectTab(TAB_CASH);
		updateAmounts();
	}

	public void show() {
		dialog.show();
	}

	public void dismiss() {
		if (dialog.isShowing()) dialog.dismiss();
	}

	public boolean isShowing() {
		return dialog.isShowing();
	}

	public void setBusy(boolean busy) {
		this.busy = busy;
		dialog.setCancelable(!busy);
		progress.setVisibility(busy ? View.VISIBLE : View.GONE);
		confirm.setEnabled(!busy);
		cancel.setEnabled(!busy);
		if (busy) error.setVisibility(View.GONE);
	}

	public void showError(String message) {
		error.setText(TextUtils.isEmpty(message) ? BA.str(R.string.error_word) : message);
		error.setVisibility(View.VISIBLE);
	}

	private void selectTab(int index) {
		if (busy) return;
		tab = index;
		for (int i = 0; i < tabs.length; i++) tabs[i].setSelected(i == index);
		error.setVisibility(View.GONE);
		cashLayout.setVisibility(index == TAB_CASH ? View.VISIBLE : View.GONE);
		qrLayout.setVisibility(index == TAB_QR ? View.VISIBLE : View.GONE);
		mixedLayout.setVisibility(index == TAB_MIXED ? View.VISIBLE : View.GONE);
		if (index == TAB_QR) loadQr();
	}

	private void loadQr() {
		if (qrRequested || qrUrl == null) return;
		qrRequested = true;
		qrProgress.setVisibility(View.VISIBLE);
		qrHint.setText(BA.str(R.string.payment_kaspi_hint, Shop.money(due)));
		HttpClient.getPicasso().load(qrUrl).into(qrImage, new Callback() {
			@Override
			public void onSuccess() {
				qrProgress.setVisibility(View.GONE);
			}

			@Override
			public void onError() {
				qrProgress.setVisibility(View.GONE);
				qrHint.setText(BA.str(R.string.payment_qr_load_error));
				qrRequested = false; // tried again when the tab is opened next
			}
		});
	}

	private void updateAmounts() {
		long received = Shop.number(cashReceived);
		if (received > 0) {
			cashChange.setVisibility(View.VISIBLE);
			cashChange.setText(BA.str(R.string.shop_change, Shop.money(Math.max(0, received - due))));
			cashChange.setTextColor(BA.getContext().getResources().getColor(
					received >= due ? R.color.BlueLight2 : R.color.Orange));
		} else {
			cashChange.setVisibility(View.GONE);
		}
		long remaining = due - mixedSum();
		mixedRemaining.setText(BA.str(R.string.balance_sp) + Shop.money(remaining));
		mixedRemaining.setTextColor(BA.getContext().getResources().getColor(
				remaining == 0 ? R.color.BlueLight2 : R.color.Orange));
	}

	private long mixedSum() {
		return amount(mixedCash) + amount(mixedCard) + amount(mixedQr);
	}

	private void submit() {
		if (busy) return;
		List<BookInfo.PriceDetail> parts = new ArrayList<>();
		switch (tab) {
			case TAB_CARD:
				parts.add(part(Constants.PAYMENT_CARD, due));
				break;
			case TAB_QR:
				parts.add(part(Constants.PAYMENT_KASPI_QR, due));
				break;
			case TAB_MIXED:
				if (mixedSum() != due) {
					showError(BA.str(R.string.balance_not_zero));
					return;
				}
				addPart(parts, Constants.PAYMENT_CASH, amount(mixedCash));
				addPart(parts, Constants.PAYMENT_CARD, amount(mixedCard));
				addPart(parts, Constants.PAYMENT_KASPI_QR, amount(mixedQr));
				break;
			default:
				long received = Shop.number(cashReceived);
				if (received >= 0 && received < due && !cashReceived.getText().toString().trim().isEmpty()) {
					showError(BA.str(R.string.shop_received_less));
					return;
				}
				parts.add(part(Constants.PAYMENT_CASH, due));
		}
		listener.onSubmit(parts);
	}

	/** A field's whole amount, capped by the backend's limit; a hidden row counts as 0. */
	private static long amount(EditText field) {
		View row = (View) field.getParent();
		if (row.getVisibility() != View.VISIBLE) return 0;
		long value = Shop.number(field);
		return value < 0 ? 0 : Math.min(value, Shop.MAX_MONEY);
	}

	private static void addPart(List<BookInfo.PriceDetail> parts, String type, long amount) {
		if (amount > 0) parts.add(part(type, amount));
	}

	private static BookInfo.PriceDetail part(String type, long amount) {
		BookInfo.PriceDetail p = new BookInfo.PriceDetail();
		p.setPaymentType(type);
		p.setAmount((double) amount);
		return p;
	}
}
