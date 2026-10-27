package com.driverspa.admin.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.shop.Shop;
import com.driverspa.assist.BaseAssist;
import com.driverspa.model.ShopItem;
import com.driverspa.model.api.request.ShopItemRequest;
import com.driverspa.util.Constants;
import com.driverspa.util.Converters;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.ws.AdminShopItemSaveRequestEvent;
import com.driverspa.util.otto.ws.AdminShopItemSaveResponseEvent;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * The add / edit form of a shop item (docs/shop.md): name, category (an existing one or a new
 * one), price, "Учитывать остаток" with the starting quantity, its purchase price and how it was
 * paid (new items only) and the low-stock threshold, and "Показывать в продаже".
 */
public class AdminShopItemActivity extends AdminBaseActivity {

	public static final String EXTRA_WASHER_ID = "EXTRA_WASHER_ID";
	public static final String EXTRA_ITEM = "EXTRA_ITEM";
	public static final String EXTRA_CATEGORIES = "EXTRA_CATEGORIES";
	private static final String STATE_PAYMENT = "STATE_PAYMENT";

	@BindView(R.id.name)
	EditText name;
	@BindView(R.id.category)
	AutoCompleteTextView category;
	@BindView(R.id.price)
	EditText price;
	@BindView(R.id.trackStock)
	SwitchCompat trackStock;
	@BindView(R.id.stockLayout)
	View stockLayout;
	@BindView(R.id.initialLayout)
	View initialLayout;
	@BindView(R.id.initialStock)
	EditText initialStock;
	@BindView(R.id.unitCost)
	EditText unitCost;
	@BindView(R.id.paidBy)
	LinearLayout paidBy;
	@BindView(R.id.lowStock)
	EditText lowStock;
	@BindView(R.id.active)
	SwitchCompat active;
	@BindView(R.id.error)
	TextView error;
	@BindView(R.id.save)
	Button save;

	private String washerId;
	private ShopItem item;
	private String payment = Constants.PAYMENT_CASH;
	private boolean awaitingSave;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_admin_shop_item);
		ButterKnife.bind(this);
		washerId = getIntent().getStringExtra(EXTRA_WASHER_ID);
		String json = getIntent().getStringExtra(EXTRA_ITEM);
		if (!TextUtils.isEmpty(json)) {
			try {
				item = Converters.gsonWithDate().fromJson(json, ShopItem.class);
			} catch (Exception e) {
				item = null;
			}
		}
		if (savedInstanceState != null) payment = savedInstanceState.getString(STATE_PAYMENT, payment);

		Toolbar toolbar = findViewById(R.id.toolbar_actionbar);
		((TextView) toolbar.findViewById(R.id.action_bar_title)).setText(
				BA.str(item == null ? R.string.shop_item_new : R.string.shop_item_edit));
		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowTitleEnabled(false);
		}

		ArrayList<String> categories = getIntent().getStringArrayListExtra(EXTRA_CATEGORIES);
		if (categories == null) categories = new ArrayList<>();
		category.setAdapter(new ArrayAdapter<>(this, R.layout.home_spinner_dropdown_item, categories));
		category.setOnFocusChangeListener((v, hasFocus) -> {
			if (hasFocus && category.getAdapter() != null && category.getAdapter().getCount() > 0) category.showDropDown();
		});
		category.setOnClickListener(v -> {
			if (category.getAdapter() != null && category.getAdapter().getCount() > 0) category.showDropDown();
		});

		buildPaidBy();
		// The starting stock is a purchase made with the new item; later stock comes from Приход.
		initialLayout.setVisibility(item == null ? View.VISIBLE : View.GONE);
		if (item != null && savedInstanceState == null) {
			name.setText(item.getName());
			category.setText(item.getCategory() != null ? item.getCategory() : "", false);
			price.setText(String.valueOf(item.getPrice()));
			trackStock.setChecked(item.isTrackStock());
			if (item.getLowStock() > 0) lowStock.setText(String.valueOf(item.getLowStock()));
			active.setChecked(item.isActive());
		}
		trackStock.setOnCheckedChangeListener((b, checked) -> updateStockLayout());
		updateStockLayout();
		save.setOnClickListener(v -> submit());
		installFinishOnBackCallback();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_PAYMENT, payment);
	}

	@Override
	public void onResume() {
		super.onResume();
		BA.getEventBus().register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		BA.getEventBus().unregister(this);
		if (awaitingSave) {
			awaitingSave = false;
			setWaitScreen(false);
			if (item == null) {
				// A new item (maybe with its starting purchase) may have been saved: don't offer
				// to send it again, ask to look at the list instead.
				ToastUtil.display(this, BA.str(R.string.shop_check_items));
				finish();
				return;
			}
			save.setEnabled(true); // an edit can safely be sent again
		}
	}

	private void updateStockLayout() {
		stockLayout.setVisibility(trackStock.isChecked() ? View.VISIBLE : View.GONE);
	}

	private void buildPaidBy() {
		String[] types = {Constants.PAYMENT_CASH, Constants.PAYMENT_CARD, Shop.PAY_TRANSFER};
		int[] titles = {R.string.shop_paid_cash, R.string.shop_paid_card, R.string.shop_paid_transfer};
		paidBy.removeAllViews();
		for (int i = 0; i < types.length; i++) {
			final String type = types[i];
			TextView chip = (TextView) LayoutInflater.from(this).inflate(R.layout.item_shop_chip, paidBy, false);
			chip.setText(BA.str(titles[i]));
			chip.setSelected(type.equals(payment));
			chip.setOnClickListener(v -> {
				payment = type;
				for (int j = 0; j < paidBy.getChildCount(); j++) paidBy.getChildAt(j).setSelected(false);
				v.setSelected(true);
			});
			paidBy.addView(chip);
		}
	}

	private void submit() {
		if (awaitingSave || washerId == null) return;
		error.setVisibility(View.GONE);
		String nameText = name.getText().toString().trim();
		if (nameText.isEmpty()) {
			name.setError(BA.str(R.string.shop_name_required));
			name.requestFocus();
			return;
		}
		long priceValue = Shop.number(price);
		if (priceValue < 0) {
			price.setError(BA.str(R.string.shop_price_required));
			price.requestFocus();
			return;
		}
		if (priceValue > Shop.MAX_MONEY) {
			price.setError(BA.str(R.string.shop_err_price));
			price.requestFocus();
			return;
		}

		ShopItemRequest request = new ShopItemRequest();
		request.name = nameText;
		String categoryText = category.getText().toString().trim();
		if (item == null) {
			request.carwash = washerId;
			if (!categoryText.isEmpty()) request.category = categoryText;
		} else {
			request.category = categoryText; // "" clears it
		}
		request.price = priceValue;
		request.trackStock = trackStock.isChecked();
		request.isActive = active.isChecked();

		if (trackStock.isChecked()) {
			long low = Shop.number(lowStock);
			if (low > Shop.MAX_OTHER) {
				lowStock.setError(BA.str(R.string.shop_err_low_stock));
				lowStock.requestFocus();
				return;
			}
			request.lowStock = (int) Math.max(0, low);
			if (item == null) {
				long initial = Shop.number(initialStock);
				if (initial > Shop.MAX_QTY) {
					initialStock.setError(BA.str(R.string.shop_err_qty));
					initialStock.requestFocus();
					return;
				}
				if (initial > 0) {
					double unit = Shop.decimal(unitCost);
					if (unit < 0) {
						unitCost.setError(BA.str(R.string.shop_unit_cost_required));
						unitCost.requestFocus();
						return;
					}
					if (unit > Shop.MAX_MONEY) {
						unitCost.setError(BA.str(R.string.shop_err_unit_cost));
						unitCost.requestFocus();
						return;
					}
					request.initialStock = (int) initial;
					request.unitCost = unit;
					request.paymentType = payment;
				}
			}
		} else if (item != null) {
			request.lowStock = item.getLowStock();
		}

		awaitingSave = true;
		save.setEnabled(false);
		setWaitScreen(true);
		BA.getEventBus().post(new AdminShopItemSaveRequestEvent(item != null ? item.getId() : null, request));
	}

	@Subscribe
	public void onItemSaved(AdminShopItemSaveResponseEvent event) {
		if (!awaitingSave) return;
		awaitingSave = false;
		save.setEnabled(true);
		setWaitScreen(false);
		if (event.getResult() == null) return; // the connection error is shown by BA
		if (!BaseAssist.isSuccess(event.getResult())) {
			error.setText(Shop.error(event.getResult()));
			error.setVisibility(View.VISIBLE);
			return;
		}
		ToastUtil.display(this, BA.str(R.string.shop_item_saved));
		setResult(RESULT_OK);
		finish();
		overridePendingTransitionWithCommonCloseTransition();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		if (menuItem.getItemId() == android.R.id.home) {
			finish();
			overridePendingTransitionWithCommonCloseTransition();
			return true;
		}
		return super.onOptionsItemSelected(menuItem);
	}

	private ProgressDialog progress;

	/** A wait screen that Back can't close, so the item can't be sent twice. */
	@Override
	public void setWaitScreen(boolean set) {
		if (isFinishing() || isDestroyed()) return;
		if (progress == null) {
			progress = new ProgressDialog(this);
			progress.setTitle("");
			progress.setIndeterminate(true);
			progress.setCancelable(false);
			progress.setMessage(BA.str(R.string.loading_dots));
		}
		if (set) progress.show();
		else if (progress.isShowing()) progress.dismiss();
	}
}
