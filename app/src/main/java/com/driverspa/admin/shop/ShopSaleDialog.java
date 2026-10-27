package com.driverspa.admin.shop;

import android.app.Activity;
import android.app.ProgressDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.appcompat.app.AlertDialog;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.assist.BaseAssist;
import com.driverspa.model.ShopSale;
import com.driverspa.model.api.response.ShopSaleResponseHolder;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.ws.AdminShopSaleCancelRequestEvent;
import com.driverspa.util.otto.ws.AdminShopSaleCancelResponseEvent;
import com.driverspa.util.otto.ws.AdminShopSaleRequestEvent;
import com.driverspa.util.otto.ws.AdminShopSaleResponseEvent;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.squareup.otto.Subscribe;

/**
 * A sale's details: its lines and payment; managers can cancel it with a reason and a
 * confirmation. Opened from Продажи, and from a shop transaction in the finance lists (by id).
 * Listens on the bus only while a request of its own is under way.
 */
public class ShopSaleDialog {

	private final Activity activity;
	private final boolean manager;
	private ProgressDialog progress;
	private String awaitingSale;
	private boolean awaitingCancel;
	private boolean registered;

	private ShopSaleDialog(Activity activity, boolean manager) {
		this.activity = activity;
		this.manager = manager;
	}

	public static void show(Activity activity, ShopSale sale, boolean manager) {
		new ShopSaleDialog(activity, manager).showSale(sale);
	}

	/** Loads the sale, then shows it. */
	public static void open(Activity activity, String saleId, boolean manager) {
		ShopSaleDialog dialog = new ShopSaleDialog(activity, manager);
		dialog.awaitingSale = saleId;
		dialog.register();
		dialog.showProgress(true);
		BA.getEventBus().post(new AdminShopSaleRequestEvent(saleId));
	}

	private void register() {
		if (registered) return;
		BA.getEventBus().register(this);
		registered = true;
	}

	private void unregister() {
		if (!registered) return;
		BA.getEventBus().unregister(this);
		registered = false;
	}

	private boolean alive() {
		return !activity.isFinishing() && !activity.isDestroyed();
	}

	@Subscribe
	public void onSaleReceived(AdminShopSaleResponseEvent event) {
		if (awaitingSale == null || !awaitingSale.equals(event.getSaleId())) return;
		awaitingSale = null;
		unregister();
		showProgress(false);
		ShopSaleResponseHolder result = event.getResult();
		if (result == null || !alive()) return;
		if (!BaseAssist.isSuccess(result) || result.getResponse() == null) {
			ToastUtil.display(activity, Shop.error(result));
			return;
		}
		showSale(result.getResponse());
	}

	private void showSale(final ShopSale sale) {
		if (!alive()) return;
		StringBuilder text = new StringBuilder();
		text.append(Shop.dateTime(sale.getTs()));
		if (!TextUtils.isEmpty(sale.getUserName())) text.append('\n').append(BA.str(R.string.shop_seller, sale.getUserName()));
		text.append("\n");
		if (sale.getLines() != null) {
			for (ShopSale.Line line : sale.getLines()) {
				text.append('\n').append(line.getName()).append("  ")
						.append(Shop.money(line.getPrice())).append(" × ").append(line.getQty())
						.append(" = ").append(Shop.money(line.getSum()));
			}
		}
		text.append("\n\n").append(BA.str(R.string.total_colon)).append(' ').append(Shop.money(sale.getTotal()));
		String payment = Shop.paymentSummary(sale.getPriceDetails());
		if (!payment.isEmpty()) text.append('\n').append(BA.str(R.string.shop_payment_line, payment));
		if (sale.getCost() != null && sale.getProfit() != null) {
			text.append('\n').append(BA.str(R.string.shop_cost_line, Shop.money(sale.getCost()), Shop.money(sale.getProfit())));
		}
		if (sale.isCanceled()) {
			text.append("\n\n").append(BA.str(R.string.shop_status_canceled));
			if (sale.getCanceledTs() != null) text.append(' ').append(Shop.dateTime(sale.getCanceledTs()));
			if (!TextUtils.isEmpty(sale.getCanceledByName())) {
				text.append('\n').append(BA.str(R.string.shop_canceled_by, sale.getCanceledByName()));
			}
			if (!TextUtils.isEmpty(sale.getCancelReason())) {
				text.append('\n').append(BA.str(R.string.shop_reason_line, sale.getCancelReason()));
			}
		}

		AlertDialog.Builder builder = new MaterialAlertDialogBuilder(activity)
				.setTitle(BA.str(R.string.shop_sale_title, sale.getNumber()))
				.setMessage(text.toString())
				.setPositiveButton(BA.str(R.string.shop_close), null);
		if (manager && sale.isPaid()) {
			builder.setNeutralButton(BA.str(R.string.shop_cancel_sale), (d, which) -> askReason(sale));
		}
		builder.show();
	}

	private void askReason(final ShopSale sale) {
		if (!alive()) return;
		final EditText field = new EditText(activity);
		field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		field.setFilters(new InputFilter[]{new InputFilter.LengthFilter(300)});
		field.setHint(BA.str(R.string.shop_reason));
		field.setTextColor(activity.getResources().getColor(R.color.White));
		FrameLayout box = new FrameLayout(activity);
		int pad = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, activity.getResources().getDisplayMetrics());
		box.setPadding(pad, pad / 2, pad, 0);
		box.addView(field);

		final AlertDialog dialog = new MaterialAlertDialogBuilder(activity)
				.setTitle(BA.str(R.string.shop_cancel_sale_q, sale.getNumber()))
				.setView(box)
				.setNegativeButton(BA.str(R.string.no_word), null)
				.setPositiveButton(BA.str(R.string.shop_cancel_sale), null)
				.create();
		dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
			String reason = field.getText().toString().trim();
			if (reason.isEmpty()) {
				field.setError(BA.str(R.string.shop_reason_required));
				return;
			}
			if (awaitingCancel) return;
			dialog.dismiss();
			awaitingCancel = true;
			register();
			showProgress(true);
			BA.getEventBus().post(new AdminShopSaleCancelRequestEvent(sale.getId(), reason));
		}));
		dialog.show();
	}

	@Subscribe
	public void onSaleCanceled(AdminShopSaleCancelResponseEvent event) {
		if (!awaitingCancel) return;
		awaitingCancel = false;
		unregister();
		showProgress(false);
		if (event.getResult() == null || !alive()) return;
		if (!BaseAssist.isSuccess(event.getResult())) {
			ToastUtil.display(activity, Shop.error(event.getResult()));
			return;
		}
		ToastUtil.display(activity, BA.str(R.string.shop_sale_canceled));
	}

	private void showProgress(boolean show) {
		if (!alive()) return;
		if (progress == null) {
			progress = new ProgressDialog(activity);
			progress.setTitle("");
			progress.setIndeterminate(true);
			progress.setCancelable(false);
			progress.setMessage(BA.str(R.string.loading_dots));
		}
		if (show) progress.show();
		else if (progress.isShowing()) progress.dismiss();
	}
}
