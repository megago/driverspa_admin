package com.driverspa.admin.shift;

import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.activity.AdminBookingInfoActivity;
import com.driverspa.admin.activity.AdminShiftReportActivity;
import com.driverspa.model.shift.Shift;
import com.driverspa.model.shift.ShiftReport;
import com.driverspa.model.shift.ShiftResponses;
import com.driverspa.util.Constants;
import com.driverspa.util.ToastUtil;

/**
 * Закрытие смены (docs/shift.md): (1) Проверка: cars still in a box and finished orders not paid
 * (a warning, not a block), and the payouts to washers from the drawer; (2) Пересчёт: one amount,
 * nothing expected shown; (3) Итог: expected, counted, the difference, the comment a difference
 * above the limit needs, the cash left in the drawer and what is handed over. "Cash moved since
 * the count" sends the user back to the count. After the close, the Z-report opens.
 */
public final class ShiftCloseWizard {

	public interface OnDone {
		void onDone();
	}

	private final Activity activity;
	private final Shift shift;
	private final OnDone onDone;
	private final LinearLayout body;
	private AlertDialog dialog;
	private ShiftReport report;
	private ShiftResponses.CountResult counted;
	private boolean busy;

	private ShiftCloseWizard(Activity activity, Shift shift, OnDone onDone) {
		this.activity = activity;
		this.shift = shift;
		this.onDone = onDone;
		this.body = ShiftUi.column(activity, 20);
	}

	public static void start(Activity activity, Shift shift, OnDone onDone) {
		new ShiftCloseWizard(activity, shift, onDone).show();
	}

	private void show() {
		dialog = ShiftUi.dialog(activity)
				.setTitle(BA.str(R.string.shift_close_title, shift.getNumberText()))
				.setView(ShiftUi.scroll(activity, body))
				.setCancelable(false)
				.create();
		// Not dismissed by a hardware keyboard's Escape either (it bypasses setCancelable).
		dialog.setOnKeyListener((d, keyCode, event) -> keyCode == android.view.KeyEvent.KEYCODE_ESCAPE);
		dialog.show();
		stepCheck();
	}

	private boolean alive() {
		return ShiftUi.alive(activity) && dialog != null && dialog.isShowing();
	}

	private void dismiss() {
		if (dialog != null && dialog.isShowing()) dialog.dismiss();
	}

	/** "1. Проверка › 2. Пересчёт › 3. Итог" with the current step bold and white. */
	private void steps(int current) {
		String[] names = {BA.str(R.string.shift_step_check), BA.str(R.string.shift_step_count), BA.str(R.string.shift_step_result)};
		android.text.SpannableStringBuilder b = new android.text.SpannableStringBuilder();
		for (int i = 0; i < names.length; i++) {
			if (i > 0) b.append("  ›  ");
			int start = b.length();
			b.append(String.valueOf(i + 1)).append(". ").append(names[i]);
			if (i == current) {
				b.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), start, b.length(), 0);
				b.setSpan(new android.text.style.ForegroundColorSpan(androidx.core.content.ContextCompat.getColor(activity, R.color.White)),
						start, b.length(), 0);
			}
		}
		TextView t = ShiftUi.text(activity, "", R.color.BlueLight, R.dimen.text_small);
		t.setText(b);
		t.setPadding(0, ShiftUi.dp(activity, 4), 0, ShiftUi.dp(activity, 8));
		body.addView(t);
	}

	// ---- 1. check: open orders and washer payouts ----

	private void stepCheck() {
		body.removeAllViews();
		steps(0);
		body.addView(ShiftUi.value(activity, BA.str(R.string.loading_dots)));
		ShiftApi.report(shift.getId(), (r, err) -> {
			if (!alive()) return;
			report = r;
			renderCheck(err);
		});
	}

	private void renderCheck(String loadError) {
		body.removeAllViews();
		steps(0);
		if (report == null) {
			body.addView(ShiftUi.value(activity, loadError != null ? loadError : BA.str(R.string.shift_load_error)));
		} else {
			ShiftReport.OpenOrders orders = report.getOpenOrders();
			if (orders == null || orders.isEmpty()) {
				body.addView(ShiftUi.value(activity, BA.str(R.string.shift_no_open_orders)));
			} else {
				if (orders.getInBox() != null && !orders.getInBox().isEmpty()) {
					body.addView(ShiftUi.text(activity, BA.str(R.string.shift_in_box, orders.getInBox().size()), R.color.Orange, R.dimen.text_normal));
					for (ShiftReport.Order o : orders.getInBox()) {
						body.addView(orderLink(o, (o.getBoxName() != null ? o.getBoxName() + " · " : "") + Shifts.time(o.getTime())));
					}
				}
				if (orders.getUnpaid() != null && !orders.getUnpaid().isEmpty()) {
					body.addView(ShiftUi.text(activity, BA.str(R.string.shift_unpaid, orders.getUnpaid().size()), R.color.Orange, R.dimen.text_normal));
					for (ShiftReport.Order o : orders.getUnpaid()) body.addView(orderLink(o, Shifts.money(o.getPrice())));
				}
				body.addView(ShiftUi.hint(activity, BA.str(R.string.shift_open_orders_hint)));
			}
			renderPayouts();
		}
		TextView cancel = ShiftUi.button(activity, BA.str(R.string.cancel_word), false);
		TextView next = ShiftUi.button(activity, BA.str(R.string.next), true);
		cancel.setOnClickListener(v -> {
			if (!busy) dismiss();
		});
		next.setOnClickListener(v -> {
			if (!busy) stepCount(null);
		});
		body.addView(ShiftUi.buttons(activity, cancel, next));
	}

	private TextView orderLink(final ShiftReport.Order o, String extra) {
		TextView t = ShiftUi.text(activity, "• " + o.getClientKey() + " · " + extra, R.color.BlueLight, R.dimen.text_normal);
		t.setPadding(0, ShiftUi.dp(activity, 6), 0, ShiftUi.dp(activity, 6));
		t.setOnClickListener(v -> activity.startActivity(new Intent(activity, AdminBookingInfoActivity.class)
				.putExtra(Constants.EXTRA_BOOKING_ID, o.getId())));
		return t;
	}

	/** Washers with pay: earned / paid and an amount (earned − paid), "Выдать" posts a payout. */
	private void renderPayouts() {
		if (report.getWashers() == null) return;
		boolean any = false;
		for (final ShiftReport.WasherPay w : report.getWashers()) {
			if (w.getEarned() <= 0 && w.getPaid() <= 0) continue;
			if (!any) {
				body.addView(ShiftUi.label(activity, BA.str(R.string.shift_payouts)));
				body.addView(ShiftUi.hint(activity, BA.str(R.string.shift_payouts_hint)));
				any = true;
			}
			body.addView(ShiftUi.value(activity, w.getName()));
			body.addView(ShiftUi.hint(activity, BA.str(R.string.shift_earned_paid, Shifts.money(w.getEarned()), Shifts.money(w.getPaid()))));
			final EditText amount = ShiftUi.amountField(activity);
			double due = Math.max(0, Math.round((w.getEarned() - w.getPaid()) * 100) / 100.0);
			if (due > 0) amount.setText(Shifts.plain(due));
			final TextView pay = ShiftUi.button(activity, BA.str(R.string.shift_pay_out), false);
			LinearLayout row = ShiftUi.buttons(activity, new TextView[0]);
			LinearLayout.LayoutParams fieldLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			amount.setLayoutParams(fieldLp);
			LinearLayout.LayoutParams payLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			payLp.leftMargin = ShiftUi.dp(activity, 8);
			pay.setLayoutParams(payLp);
			row.addView(amount);
			row.addView(pay);
			body.addView(row);
			pay.setOnClickListener(v -> {
				Double value = Shifts.parseCash(amount.getText().toString());
				if (busy || value == null || value <= 0) return;
				busy = true;
				ShiftApi.cash(shift.getId(), "payout", value, BA.str(R.string.shift_payout_reason), w.getStaffUid(),
						Shifts.newClientUid(), (data, err) -> {
							busy = false;
							if (!alive()) return;
							ToastUtil.display(activity, data != null ? BA.str(R.string.shift_cash_saved, Shifts.money(value)) : err);
							if (data != null) stepCheck();
						});
			});
		}
	}

	// ---- 2. the blind count ----

	private void stepCount(String error) {
		body.removeAllViews();
		steps(1);
		body.addView(ShiftUi.label(activity, BA.str(R.string.shift_count_label)));
		final EditText amount = ShiftUi.amountField(activity);
		body.addView(amount);
		body.addView(ShiftUi.hint(activity, BA.str(R.string.shift_count_hint)));
		final TextView errorView = ShiftUi.error(activity);
		body.addView(errorView);
		ShiftUi.showError(errorView, error);
		TextView back = ShiftUi.button(activity, BA.str(R.string.back), false);
		final TextView next = ShiftUi.button(activity, BA.str(R.string.next), true);
		back.setOnClickListener(v -> {
			if (!busy) renderCheck(null);
		});
		next.setOnClickListener(v -> {
			Double value = Shifts.parseCash(amount.getText().toString());
			if (value == null) {
				ShiftUi.showError(errorView, BA.str(R.string.shift_err_amount));
				return;
			}
			if (busy) return;
			busy = true;
			next.setEnabled(false);
			ShiftApi.count(shift.getId(), value, (result, err) -> {
				busy = false;
				next.setEnabled(true);
				if (!alive()) return;
				if (result == null) {
					ShiftUi.showError(errorView, err);
					return;
				}
				counted = result;
				stepResult();
			});
		});
		body.addView(ShiftUi.buttons(activity, back, next));
		amount.requestFocus();
	}

	// ---- 3. the result: difference, comment, cash left ----

	private void stepResult() {
		body.removeAllViews();
		steps(2);
		final ShiftResponses.CountResult c = counted;
		body.addView(ShiftUi.row(activity, BA.str(R.string.shift_expected), Shifts.money(c.getExpectedCash()), R.color.White, true));
		body.addView(ShiftUi.row(activity, BA.str(R.string.shift_counted), Shifts.money(c.getCountedCash()), R.color.White, true));
		body.addView(ShiftUi.row(activity, BA.str(R.string.shift_difference),
				Shifts.signed(c.getDifference()) + " · " + Shifts.toneText(c.getDifference()), Shifts.toneColor(c.getDifference()), true));

		body.addView(ShiftUi.label(activity, BA.str(R.string.comment)
				+ (c.isCommentRequired() ? "" : " (" + BA.str(R.string.shift_optional) + ")")));
		final EditText comment = ShiftUi.textField(activity, "");
		body.addView(comment);
		if (c.isCommentRequired()) body.addView(ShiftUi.hint(activity, BA.str(R.string.shift_comment_hint, Shifts.money(c.getLimit()))));

		body.addView(ShiftUi.label(activity, BA.str(R.string.shift_left)));
		final EditText left = ShiftUi.amountField(activity);
		left.setText(Shifts.plain(Math.min(shift.getOpeningFloat(), c.getCountedCash())));
		body.addView(left);
		final TextView removed = ShiftUi.hint(activity, "");
		body.addView(removed);
		TextWatcher watcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				Double value = Shifts.parseCash(s.toString());
				if (value == null || value > c.getCountedCash()) {
					removed.setVisibility(View.GONE);
				} else {
					removed.setText(BA.str(R.string.shift_removed_is, Shifts.money(c.getCountedCash() - value)));
					removed.setVisibility(View.VISIBLE);
				}
			}
		};
		left.addTextChangedListener(watcher);
		watcher.afterTextChanged(left.getText());

		final TextView errorView = ShiftUi.error(activity);
		body.addView(errorView);
		TextView recount = ShiftUi.button(activity, BA.str(R.string.shift_recount), false);
		final TextView close = ShiftUi.button(activity, BA.str(R.string.shift_close), true);
		recount.setOnClickListener(v -> {
			if (!busy) stepCount(null);
		});
		close.setOnClickListener(v -> {
			String text = comment.getText().toString().trim();
			if (c.isCommentRequired() && text.isEmpty()) {
				ShiftUi.showError(errorView, BA.str(R.string.shift_err_comment));
				return;
			}
			Double value = Shifts.parseCash(left.getText().toString());
			if (value == null || value > c.getCountedCash()) {
				ShiftUi.showError(errorView, BA.str(R.string.shift_err_left));
				return;
			}
			if (busy) return;
			busy = true;
			close.setEnabled(false);
			ShiftApi.close(shift.getId(), value, text, (closed, err) -> {
				busy = false;
				close.setEnabled(true);
				if (!alive()) return;
				if (closed == null) {
					if (BA.str(R.string.shift_err_moved).equals(err)) {
						// cash moved after the count: count again
						stepCount(err);
					} else {
						ShiftUi.showError(errorView, err);
					}
					return;
				}
				dismiss();
				ToastUtil.display(activity, BA.str(R.string.shift_closed_toast));
				activity.startActivity(new Intent(activity, AdminShiftReportActivity.class)
						.putExtra(AdminShiftReportActivity.EXTRA_SHIFT_ID, closed.getId()));
				if (onDone != null) onDone.onDone();
			});
		});
		body.addView(ShiftUi.buttons(activity, recount, close));
	}
}
