package com.driverspa.admin.payment;

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
import com.driverspa.model.ClientInfo;
import com.driverspa.util.Constants;
import com.driverspa.util.HttpClient;
import com.squareup.picasso.Callback;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * The payment window: how the client pays the order's price. Tabs Карта (opened first; it shows
 * the car wash's QR, Kaspi, Halyk..., since a QR payment is a card payment) / Наличные / Депозит
 * (only when the client's deposit covers the price) / Смешанная. The amount due is the order's
 * price from the backend, not recomputed. QR as a type of its own is left for older orders only.
 */
class PaymentDialog {

	interface Listener {
		void onSubmit(List<BookInfo.PriceDetail> parts);

		void onCancel();
	}

	private static final int TAB_CASH = 0;
	private static final int TAB_CARD = 1;
	private static final int TAB_QR = 2;
	private static final int TAB_DEPOSIT = 3;
	private static final int TAB_MIXED = 4;
	/** On account of a corporate client: shown first when the car is in a company's list. */
	private static final int TAB_CORPORATE = 5;

	private final Dialog dialog;
	private final Listener listener;
	private final long due;
	/** The order's price, or the company's contract price on the corporate tab. */
	private final long corporateDue;
	private final String corporateName;
	private final TextView amountView;
	private final String qrUrl;
	private final double depositBalance;
	private final double bonusBalance;
	private int tab = TAB_CARD;
	private boolean busy;
	private boolean qrRequested;

	private final View[] tabs;
	private final TextView note, qrHint, qrAmount, mixedRemaining, error;
	private final View qrLayout, qrProgress, mixedLayout, mixedQrRow, mixedDepositRow, mixedBonusRow;
	private final ImageView qrImage;
	private final EditText mixedCash, mixedCard, mixedQr, mixedDeposit, mixedBonus;
	private final Button cancel, confirm;
	private final View progress;

	/**
	 * @param finishing the order is finished after paying ("Оплачено и завершить"), not only paid
	 */
	PaymentDialog(Activity activity, BookInfo book, String qrUrl, String corporateName, Double corporatePrice,
				  boolean finishing, Listener listener) {
		this.listener = listener;
		this.qrUrl = qrUrl;
		this.due = Math.max(0, Math.round(PaymentFlow.price(book)));
		this.corporateName = corporateName;
		this.corporateDue = corporatePrice != null ? Math.max(0, Math.round(corporatePrice)) : due;
		ClientInfo client = book.getCompanyClient();
		depositBalance = client != null && client.getDepositAmount() != null ? Math.max(0, client.getDepositAmount()) : 0;
		bonusBalance = client != null && client.getAccruedBonuses() != null ? Math.max(0, client.getAccruedBonuses()) : 0;

		View root = LayoutInflater.from(activity).inflate(R.layout.dialog_payment, null);
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

		((TextView) root.findViewById(R.id.paymentSubtitle)).setText(carNumber(book.getClientKey()));
		amountView = root.findViewById(R.id.paymentAmount);
		amountView.setText(money(due));
		tabs = new View[]{root.findViewById(R.id.tabCash), root.findViewById(R.id.tabCard),
				root.findViewById(R.id.tabQr), root.findViewById(R.id.tabDeposit), root.findViewById(R.id.tabMixed),
				root.findViewById(R.id.tabCorporate)};
		tabs[TAB_CORPORATE].setVisibility(corporateName != null ? View.VISIBLE : View.GONE);
		note = root.findViewById(R.id.paymentNote);
		qrLayout = root.findViewById(R.id.qrLayout);
		qrImage = root.findViewById(R.id.qrImage);
		qrProgress = root.findViewById(R.id.qrProgress);
		qrHint = root.findViewById(R.id.qrHint);
		qrAmount = root.findViewById(R.id.qrAmount);
		qrAmount.setText(money(due));
		mixedLayout = root.findViewById(R.id.mixedLayout);
		mixedCash = root.findViewById(R.id.mixedCash);
		mixedCard = root.findViewById(R.id.mixedCard);
		mixedQr = root.findViewById(R.id.mixedQr);
		mixedDeposit = root.findViewById(R.id.mixedDeposit);
		mixedBonus = root.findViewById(R.id.mixedBonus);
		mixedQrRow = root.findViewById(R.id.mixedQrRow);
		mixedDepositRow = root.findViewById(R.id.mixedDepositRow);
		mixedBonusRow = root.findViewById(R.id.mixedBonusRow);
		mixedRemaining = root.findViewById(R.id.mixedRemaining);
		error = root.findViewById(R.id.paymentError);
		progress = root.findViewById(R.id.paymentProgress);
		cancel = root.findViewById(R.id.paymentCancel);
		confirm = root.findViewById(R.id.paymentConfirm);
		confirm.setText(BA.str(finishing ? R.string.payment_paid_and_finish : R.string.payment_paid));

		// The split already stored on the order (made by an older app version), per type.
		long[] stored = new long[6];
		int storedTypes = 0;
		if (book.getPriceDetails() != null) {
			for (BookInfo.PriceDetail p : book.getPriceDetails()) {
				int i = tabOf(p.getPaymentType());
				long amount = p.getAmount() != null ? Math.round(p.getAmount()) : 0;
				if (i < 0 || amount <= 0) continue;
				if (stored[i] == 0) storedTypes++;
				stored[i] += amount;
			}
		}
		long storedBonus = stored[TAB_MIXED]; // tabOf maps bonuses to the "mixed" slot

		boolean depositTab = client != null && due > 0 && depositBalance >= due;
		tabs[TAB_QR].setVisibility(View.GONE);
		tabs[TAB_DEPOSIT].setVisibility(depositTab ? View.VISIBLE : View.GONE);
		// QR as a part of its own only for an older order stored that way; it is paid by card now.
		mixedQrRow.setVisibility(stored[TAB_QR] > 0 ? View.VISIBLE : View.GONE);
		mixedDepositRow.setVisibility(depositBalance > 0 || stored[TAB_DEPOSIT] > 0 ? View.VISIBLE : View.GONE);
		mixedBonusRow.setVisibility(bonusBalance > 0 || storedBonus > 0 ? View.VISIBLE : View.GONE);
		((TextView) root.findViewById(R.id.mixedDepositAvailable)).setText(BA.str(R.string.payment_available, money(depositBalance)));
		((TextView) root.findViewById(R.id.mixedBonusAvailable)).setText(BA.str(R.string.payment_available, money(bonusBalance)));

		if (due == 0 && corporateName == null) {
			// A 100 % discount: nothing to choose, the button records the payment with no parts.
			root.findViewById(R.id.paymentTabsScroll).setVisibility(View.GONE);
		}

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
				updateRemaining();
			}
		};
		for (EditText field : new EditText[]{mixedCash, mixedCard, mixedQr, mixedDeposit, mixedBonus}) {
			field.addTextChangedListener(watcher);
		}
		cancel.setOnClickListener(v -> dialog.cancel());
		confirm.setOnClickListener(v -> submit());

		// Opens on the stored split: one part gives its tab (when shown), several give "mixed".
		int start = -1;
		if (storedTypes == 1 && storedBonus == 0) {
			for (int i = TAB_CASH; i <= TAB_DEPOSIT; i++) {
				if (stored[i] == due && tabs[i].getVisibility() == View.VISIBLE) start = i;
			}
		}
		if (start < 0 && storedTypes == 0) start = TAB_CARD;
		if (start < 0) {
			start = TAB_MIXED;
			setAmount(mixedCash, stored[TAB_CASH]);
			setAmount(mixedCard, stored[TAB_CARD]);
			setAmount(mixedQr, stored[TAB_QR]);
			setAmount(mixedDeposit, stored[TAB_DEPOSIT]);
			setAmount(mixedBonus, storedBonus);
		}
		if (corporateName != null && (storedTypes == 0 || stored[TAB_CORPORATE] > 0)) start = TAB_CORPORATE;
		selectTab(due == 0 && corporateName == null ? TAB_CASH : start);
		updateRemaining();
	}

	void show() {
		dialog.show();
	}

	void dismiss() {
		if (dialog.isShowing()) dialog.dismiss();
	}

	void setBusy(boolean busy) {
		this.busy = busy;
		dialog.setCancelable(!busy);
		progress.setVisibility(busy ? View.VISIBLE : View.GONE);
		confirm.setEnabled(!busy);
		cancel.setEnabled(!busy);
		if (busy) error.setVisibility(View.GONE);
	}

	void showError(String message) {
		error.setText(TextUtils.isEmpty(message) ? BA.str(R.string.error_word) : message);
		error.setVisibility(View.VISIBLE);
	}

	private void selectTab(int index) {
		if (busy) return;
		tab = index;
		for (int i = 0; i < tabs.length; i++) tabs[i].setSelected(i == index);
		error.setVisibility(View.GONE);
		boolean single = due > 0 && index != TAB_MIXED;
		amountView.setText(money(index == TAB_CORPORATE ? corporateDue : due));
		note.setVisibility((single && index == TAB_DEPOSIT) || index == TAB_CORPORATE ? View.VISIBLE : View.GONE);
		note.setText(index == TAB_CORPORATE
				? BA.str(R.string.payment_corporate_hint, corporateName)
				: BA.str(R.string.payment_deposit_balance, money(depositBalance)));
		qrLayout.setVisibility(due > 0 && index == TAB_CARD && qrUrl != null ? View.VISIBLE : View.GONE);
		mixedLayout.setVisibility(due > 0 && index == TAB_MIXED ? View.VISIBLE : View.GONE);
		if (index == TAB_CARD) loadQr();
	}

	private void loadQr() {
		if (qrRequested || qrUrl == null) return;
		qrRequested = true;
		qrHint.setText(BA.str(R.string.payment_kaspi_hint, money(due)));
		HttpClient.getPicasso().load(qrUrl).into(qrImage, new Callback() {
			@Override
			public void onSuccess() {
				qrProgress.setVisibility(View.GONE);
			}

			@Override
			public void onError() {
				qrProgress.setVisibility(View.GONE);
				qrHint.setText(BA.str(R.string.payment_qr_load_error));
				qrRequested = false; // the next time the tab is opened tries again
			}
		});
	}

	private void updateRemaining() {
		long remaining = due - mixedSum();
		mixedRemaining.setText(BA.str(R.string.balance_sp) + money(remaining));
		mixedRemaining.setTextColor(BA.getContext().getResources().getColor(
				remaining == 0 ? R.color.BlueLight2 : R.color.Orange));
	}

	private long mixedSum() {
		return amount(mixedCash) + amount(mixedCard) + amount(mixedQr) + amount(mixedDeposit) + amount(mixedBonus);
	}

	private void submit() {
		if (busy) return;
		List<BookInfo.PriceDetail> parts = new ArrayList<>();
		if (tab == TAB_CORPORATE) {
			// The whole order on account of the company (at its contract price, if it has one).
			parts.add(part(Constants.PAYMENT_CORPORATE, corporateDue));
		} else if (due > 0) {
			switch (tab) {
				case TAB_CARD:
					parts.add(part(Constants.PAYMENT_CARD, due));
					break;
				case TAB_QR:
					parts.add(part(Constants.PAYMENT_KASPI_QR, due));
					break;
				case TAB_DEPOSIT:
					parts.add(part(Constants.PAYMENT_DEPOSIT, due));
					break;
				case TAB_MIXED:
					long deposit = amount(mixedDeposit);
					long bonus = amount(mixedBonus);
					if (deposit > depositBalance) {
						showError(BA.str(R.string.payment_not_enough_deposit));
						return;
					}
					if (bonus > bonusBalance) {
						showError(BA.str(R.string.payment_not_enough_bonuses));
						return;
					}
					if (mixedSum() != due) {
						showError(BA.str(R.string.balance_not_zero));
						return;
					}
					addPart(parts, Constants.PAYMENT_CASH, amount(mixedCash));
					addPart(parts, Constants.PAYMENT_CARD, amount(mixedCard));
					addPart(parts, Constants.PAYMENT_KASPI_QR, amount(mixedQr));
					addPart(parts, Constants.PAYMENT_DEPOSIT, deposit);
					addPart(parts, Constants.PAYMENT_BONUS, bonus);
					break;
				default:
					parts.add(part(Constants.PAYMENT_CASH, due));
			}
		}
		listener.onSubmit(parts);
	}

	/** A field's whole amount; a hidden row counts as 0. */
	private static long amount(EditText field) {
		View row = (View) field.getParent();
		if (row.getVisibility() != View.VISIBLE) return 0;
		try {
			return Math.max(0, Long.parseLong(field.getText().toString().trim()));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private static void setAmount(EditText field, long value) {
		field.setText(value > 0 ? String.valueOf(value) : "");
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

	/** The tab a stored payment type belongs to; bonuses use the "mixed" slot. */
	private static int tabOf(String type) {
		if (Constants.PAYMENT_CASH.equals(type)) return TAB_CASH;
		if (Constants.PAYMENT_CARD.equals(type)) return TAB_CARD;
		if (Constants.PAYMENT_KASPI_QR.equals(type)) return TAB_QR;
		if (Constants.PAYMENT_DEPOSIT.equals(type) || "T".equals(type)) return TAB_DEPOSIT;
		if (Constants.PAYMENT_BONUS.equals(type)) return TAB_MIXED;
		if (Constants.PAYMENT_CORPORATE.equals(type)) return TAB_CORPORATE;
		return -1;
	}

	static String money(double value) {
		return new DecimalFormat("#,###.##").format(value) + " ₸";
	}

	private static String carNumber(String clientKey) {
		if (clientKey == null) return "";
		int i = clientKey.indexOf('(');
		return (i > 0 ? clientKey.substring(0, i) : clientKey).trim();
	}
}
