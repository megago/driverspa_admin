package com.driverspa.admin.fragment;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.shop.Shop;
import com.driverspa.admin.shop.ShopPaymentDialog;
import com.driverspa.assist.BaseAssist;
import com.driverspa.model.BookInfo;
import com.driverspa.model.ShopItem;
import com.driverspa.model.ShopSale;
import com.driverspa.model.Washer;
import com.driverspa.model.api.request.ShopSaleRequest;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.ws.AdminShopItemsRequestEvent;
import com.driverspa.util.otto.ws.AdminShopItemsResponseEvent;
import com.driverspa.util.otto.ws.AdminShopSaleCreateRequestEvent;
import com.driverspa.util.otto.ws.AdminShopSaleCreateResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Продажа (docs/shop.md): a grid of tiles, one per active item; a tap adds one to the cart and a
 * long press sets the quantity. The cart is a bottom sheet on phones and a panel on the right on
 * wide screens. "Оплатить" opens the payment window; the sale is sent with a client_uid made
 * per payment attempt and reused on a retry, so a retry never sells twice.
 */
public class ShopSellFragment extends AdminBaseFragment {

	private static final String STATE_CART_IDS = "STATE_CART_IDS";
	private static final String STATE_CART_QTY = "STATE_CART_QTY";
	private static final String STATE_UID = "STATE_UID";
	private static final String STATE_CATEGORY = "STATE_CATEGORY";

	@BindView(R.id.search)
	EditText search;
	@BindView(R.id.categoryChips)
	LinearLayout categoryChips;
	@BindView(R.id.grid)
	RecyclerView grid;
	@BindView(R.id.empty)
	TextView empty;
	@BindView(R.id.progress)
	View progress;
	@BindView(R.id.cartSheet)
	View cartSheet;
	@BindView(R.id.cartHeader)
	View cartHeader;
	@BindView(R.id.cartArrow)
	ImageView cartArrow;
	@BindView(R.id.cartSummary)
	TextView cartSummary;
	@BindView(R.id.cartClear)
	View cartClear;
	@BindView(R.id.payButton)
	Button payButton;
	@BindView(R.id.cartList)
	RecyclerView cartList;

	private Unbinder unbinder;
	private BottomSheetBehavior<View> sheetBehavior;
	private Washer washer;

	private final List<ShopItem> items = new ArrayList<>();
	private final Map<String, ShopItem> itemsById = new LinkedHashMap<>();
	/** item id → quantity, in the order added */
	private final LinkedHashMap<String, Integer> cart = new LinkedHashMap<>();
	private final List<ShopItem> shown = new ArrayList<>();
	private final List<String> cartIds = new ArrayList<>();
	/** null: all categories; "" : items without a category */
	private String category;
	/** The client_uid of the current payment attempt, or null. */
	private String clientUid;
	private boolean itemsLoaded;
	private boolean awaitingSale;
	private ShopPaymentDialog paymentDialog;
	/** The last refusal of a sale, shown again if the reload then trims the cart. */
	private String lastSaleError;

	private final TileAdapter tileAdapter = new TileAdapter();
	private final CartAdapter cartAdapter = new CartAdapter();

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_shop_sell, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		unbinder = ButterKnife.bind(this, view);
		if (savedInstanceState != null) {
			String[] ids = savedInstanceState.getStringArray(STATE_CART_IDS);
			int[] qty = savedInstanceState.getIntArray(STATE_CART_QTY);
			if (ids != null && qty != null && ids.length == qty.length) {
				for (int i = 0; i < ids.length; i++) cart.put(ids[i], qty[i]);
			}
			clientUid = savedInstanceState.getString(STATE_UID);
			category = savedInstanceState.getString(STATE_CATEGORY);
		}

		final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
		grid.setLayoutManager(layoutManager);
		grid.setAdapter(tileAdapter);
		// As many columns as fit: more on tablets (shop_tile_min_width per screen size).
		grid.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
			int width = right - left - grid.getPaddingLeft() - grid.getPaddingRight();
			if (width <= 0) return;
			int columns = Math.max(2, (int) (width / getResources().getDimension(R.dimen.shop_tile_min_width)));
			if (columns != layoutManager.getSpanCount()) grid.post(() -> layoutManager.setSpanCount(columns));
		});

		cartList.setLayoutManager(new LinearLayoutManager(getActivity()));
		cartList.setAdapter(cartAdapter);
		cartList.setNestedScrollingEnabled(true);

		ViewGroup.LayoutParams params = cartSheet.getLayoutParams();
		if (params instanceof CoordinatorLayout.LayoutParams
				&& ((CoordinatorLayout.LayoutParams) params).getBehavior() instanceof BottomSheetBehavior) {
			sheetBehavior = BottomSheetBehavior.from(cartSheet);
			sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
				@Override
				public void onStateChanged(@NonNull View bottomSheet, int newState) {
					updateArrow();
				}

				@Override
				public void onSlide(@NonNull View bottomSheet, float slideOffset) {
				}
			});
			cartHeader.setOnClickListener(v -> toggleSheet());
			// The peek shows the header and the pay button: measure them once laid out.
			cartSheet.post(() -> {
				if (sheetBehavior == null || cartHeader == null) return;
				int peek = cartSheet.getPaddingTop() + cartHeader.getHeight() + payButton.getHeight()
						+ dp(4 + 8 + 4);
				if (peek > dp(60)) sheetBehavior.setPeekHeight(peek);
				updateGridPadding();
			});
		} else {
			cartArrow.setVisibility(View.GONE);
		}

		cartClear.setOnClickListener(v -> confirmClear());
		payButton.setOnClickListener(v -> openPayment());
		search.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void afterTextChanged(Editable s) {
				applyFilter();
			}
		});
		updateCart();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		String[] ids = new String[cart.size()];
		int[] qty = new int[cart.size()];
		int i = 0;
		for (Map.Entry<String, Integer> e : cart.entrySet()) {
			ids[i] = e.getKey();
			qty[i++] = e.getValue();
		}
		outState.putStringArray(STATE_CART_IDS, ids);
		outState.putIntArray(STATE_CART_QTY, qty);
		outState.putString(STATE_UID, clientUid);
		outState.putString(STATE_CATEGORY, category);
	}

	@Override
	public void onResume() {
		super.onResume();
		BA.getEventBus().register(this);
		// The answer to a sale sent before the screen was paused was missed: the window is
		// unlocked, and paying again resends the same client_uid, which can't sell twice.
		if (awaitingSale) {
			awaitingSale = false;
			if (paymentDialog != null) paymentDialog.setBusy(false);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		BA.getEventBus().unregister(this);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (paymentDialog != null) paymentDialog.dismiss();
		paymentDialog = null;
		sheetBehavior = null;
		if (unbinder != null) unbinder.unbind();
		unbinder = null;
	}

	// ---- loading ----

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		if (washer != null) loadItems();
	}

	private void loadItems() {
		if (washer == null) return;
		if (!itemsLoaded && progress != null) progress.setVisibility(View.VISIBLE);
		BA.getEventBus().post(new AdminShopItemsRequestEvent(washer.getId()));
	}

	@Subscribe
	public void onItemsReceived(AdminShopItemsResponseEvent event) {
		if (unbinder == null) return;
		progress.setVisibility(View.GONE);
		if (event.getResult() == null) return; // the connection error is shown by BA
		if (!BaseAssist.isSuccess(event.getResult()) || event.getResult().getResponse() == null) {
			ToastUtil.display(getActivity(), Shop.error(event.getResult()));
			return;
		}
		itemsLoaded = true;
		items.clear();
		itemsById.clear();
		List<ShopItem> objects = event.getResult().getResponse().getObjects();
		if (objects != null) {
			for (ShopItem item : objects) {
				if (item == null || item.getId() == null || !item.isActive()) continue;
				items.add(item);
				itemsById.put(item.getId(), item);
			}
		}
		if (fitCartToStock() && paymentDialog != null && paymentDialog.isShowing() && !awaitingSale) {
			// The window's total is no longer the cart's: close it and say why.
			paymentDialog.dismiss();
			paymentDialog = null;
			ToastUtil.display(getActivity(), lastSaleError != null ? lastSaleError : BA.str(R.string.shop_cart_trimmed));
		}
		lastSaleError = null;
		buildCategoryChips();
		applyFilter();
		updateCart();
	}

	/** Drops cart lines whose item is gone or hidden, and keeps tracked lines within the stock. */
	/** @return whether the cart changed */
	private boolean fitCartToStock() {
		boolean changed = false;
		for (String id : new ArrayList<>(cart.keySet())) {
			ShopItem item = itemsById.get(id);
			int qty = cart.get(id);
			int max = item == null ? 0 : maxQty(item);
			if (qty > max) {
				changed = true;
				if (max <= 0) cart.remove(id);
				else cart.put(id, max);
			}
		}
		if (changed) clientUid = null;
		return changed;
	}

	private static int maxQty(ShopItem item) {
		return (int) (item.isTrackStock() ? Math.min(item.getStock(), Shop.MAX_QTY) : Shop.MAX_QTY);
	}

	// ---- categories and search ----

	private void buildCategoryChips() {
		LinkedHashSet<String> categories = new LinkedHashSet<>();
		boolean uncategorized = false;
		for (ShopItem item : items) {
			if (item.getCategory() != null) categories.add(item.getCategory());
			else uncategorized = true;
		}
		if (category != null && !category.isEmpty() && !categories.contains(category)) category = null;
		if ("".equals(category) && !uncategorized) category = null;

		categoryChips.removeAllViews();
		if (categories.isEmpty()) {
			((View) categoryChips.getParent()).setVisibility(View.GONE);
			category = null;
			return;
		}
		((View) categoryChips.getParent()).setVisibility(View.VISIBLE);
		addChip(BA.str(R.string.shop_all), null);
		for (String c : categories) addChip(c, c);
		if (uncategorized) addChip(BA.str(R.string.shop_no_category), "");
	}

	private void addChip(String title, final String value) {
		TextView chip = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.item_shop_chip, categoryChips, false);
		chip.setText(title);
		chip.setSelected(TextUtils.equals(category, value));
		chip.setOnClickListener(v -> {
			category = value;
			for (int i = 0; i < categoryChips.getChildCount(); i++) categoryChips.getChildAt(i).setSelected(false);
			v.setSelected(true);
			applyFilter();
		});
		categoryChips.addView(chip);
	}

	private void applyFilter() {
		if (unbinder == null) return;
		String query = search.getText().toString().trim().toLowerCase(Locale.getDefault());
		shown.clear();
		for (ShopItem item : items) {
			if (category != null) {
				String c = item.getCategory();
				if (category.isEmpty() ? c != null : !category.equals(c)) continue;
			}
			if (!query.isEmpty() && !item.getName().toLowerCase(Locale.getDefault()).contains(query)) continue;
			shown.add(item);
		}
		tileAdapter.notifyDataSetChanged();
		empty.setVisibility(itemsLoaded && shown.isEmpty() ? View.VISIBLE : View.GONE);
	}

	// ---- the cart ----

	private void add(ShopItem item, int delta) {
		int current = cart.containsKey(item.getId()) ? cart.get(item.getId()) : 0;
		setQty(item, current + delta);
	}

	private void setQty(ShopItem item, int qty) {
		int max = maxQty(item);
		if (qty > max) {
			ToastUtil.displayShort(getActivity(), item.isTrackStock()
					? BA.str(R.string.shop_qty_max, Math.max(0, item.getStock()))
					: BA.str(R.string.shop_not_more_in_stock));
			qty = max;
		}
		if (qty <= 0) cart.remove(item.getId());
		else cart.put(item.getId(), qty);
		clientUid = null; // another cart: another payment attempt
		updateCart();
	}

	private long cartTotal() {
		long total = 0;
		for (Map.Entry<String, Integer> e : cart.entrySet()) {
			ShopItem item = itemsById.get(e.getKey());
			if (item != null) total += item.getPrice() * e.getValue();
		}
		return total;
	}

	private int cartCount() {
		int count = 0;
		for (Map.Entry<String, Integer> e : cart.entrySet()) {
			if (itemsById.containsKey(e.getKey())) count += e.getValue();
		}
		return count;
	}

	private void updateCart() {
		if (unbinder == null) return;
		cartIds.clear();
		for (String id : cart.keySet()) {
			// Lines of items not loaded yet (right after a restore) wait for the list.
			if (itemsById.containsKey(id)) cartIds.add(id);
		}
		cartAdapter.notifyDataSetChanged();
		tileAdapter.notifyDataSetChanged();
		long total = cartTotal();
		cartSummary.setText(BA.str(R.string.shop_cart_summary, cartCount(), Shop.money(total)));
		payButton.setText(BA.str(R.string.shop_pay, Shop.money(total)));
		payButton.setEnabled(!cartIds.isEmpty());
		cartClear.setVisibility(cartIds.isEmpty() ? View.INVISIBLE : View.VISIBLE);
		if (sheetBehavior != null) {
			boolean visible = !cartIds.isEmpty();
			if (!visible && sheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
				sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
			}
			cartSheet.setVisibility(visible ? View.VISIBLE : View.GONE);
			updateGridPadding();
		} else {
			cartSheet.setVisibility(View.VISIBLE);
		}
	}

	/** On phones the grid scrolls clear of the collapsed cart. */
	private void updateGridPadding() {
		if (unbinder == null || sheetBehavior == null) return;
		int bottom = cartSheet.getVisibility() == View.VISIBLE ? sheetBehavior.getPeekHeight() + dp(8) : dp(12);
		grid.setPadding(grid.getPaddingLeft(), grid.getPaddingTop(), grid.getPaddingRight(), bottom);
	}

	private void toggleSheet() {
		if (sheetBehavior == null) return;
		sheetBehavior.setState(sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED
				? BottomSheetBehavior.STATE_COLLAPSED : BottomSheetBehavior.STATE_EXPANDED);
	}

	private void updateArrow() {
		if (sheetBehavior == null || cartArrow == null) return;
		cartArrow.setImageResource(sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED
				? R.drawable.ic_arrow_drop_down_white_24dp : R.drawable.ic_arrow_drop_up_white_24dp);
	}

	private void confirmClear() {
		if (cart.isEmpty()) return;
		AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(requireActivity());
		dialog.setTitle(BA.str(R.string.shop_clear_cart) + "?");
		dialog.setNeutralButton(BA.str(R.string.no_word), null);
		dialog.setPositiveButton(BA.str(R.string.yes_word), (d, which) -> {
			cart.clear();
			clientUid = null;
			updateCart();
		});
		dialog.show();
	}

	/** Long press: type the quantity (0 removes the line). */
	private void askQuantity(final ShopItem item) {
		final EditText field = new EditText(getActivity());
		field.setInputType(InputType.TYPE_CLASS_NUMBER);
		field.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
		field.setTextColor(getResources().getColor(R.color.White));
		Integer current = cart.get(item.getId());
		field.setText(String.valueOf(current != null ? current : 1));
		field.setSelectAllOnFocus(true);
		FrameLayout box = new FrameLayout(requireActivity());
		int pad = dp(20);
		box.setPadding(pad, dp(8), pad, 0);
		box.addView(field);

		AlertDialog.Builder builder = new MaterialAlertDialogBuilder(requireActivity());
		builder.setTitle(item.getName());
		if (item.isTrackStock()) builder.setMessage(BA.str(R.string.shop_in_stock, item.getStock()));
		builder.setView(box);
		builder.setNegativeButton(BA.str(R.string.cancel_word), null);
		builder.setPositiveButton(BA.str(R.string.ok_word), null);
		final AlertDialog dialog = builder.create();
		dialog.setOnShowListener(d -> {
			field.requestFocus();
			dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
				long qty = Shop.number(field);
				if (qty < 0) {
					field.setError(BA.str(R.string.shop_qty_invalid));
					return;
				}
				if (item.isTrackStock() && qty > item.getStock()) {
					field.setError(BA.str(R.string.shop_qty_max, Math.max(0, item.getStock())));
					return;
				}
				if (qty > Shop.MAX_QTY) {
					field.setError(BA.str(R.string.shop_err_qty));
					return;
				}
				setQty(item, (int) qty);
				dialog.dismiss();
			});
		});
		dialog.show();
	}

	// ---- paying ----

	private void openPayment() {
		if (washer == null || cartIds.isEmpty() || getActivity() == null) return;
		if (paymentDialog != null && paymentDialog.isShowing()) return;
		long total = cartTotal();
		if (total > Shop.MAX_MONEY) {
			ToastUtil.display(getActivity(), BA.str(R.string.shop_err_payment_amount));
			return;
		}
		StringBuilder what = new StringBuilder();
		for (String id : cartIds) {
			ShopItem item = itemsById.get(id);
			if (item == null) continue;
			if (what.length() > 0) what.append(", ");
			what.append(item.getName()).append(" ×").append(cart.get(id));
		}
		paymentDialog = new ShopPaymentDialog(getActivity(), total, what.toString(), washer.getKaspiQrUrl(),
				new ShopPaymentDialog.Listener() {
					@Override
					public void onSubmit(List<BookInfo.PriceDetail> parts) {
						sendSale(parts);
					}

					@Override
					public void onCancel() {
						paymentDialog = null;
					}
				});
		paymentDialog.show();
	}

	private void sendSale(List<BookInfo.PriceDetail> parts) {
		if (washer == null || paymentDialog == null) return;
		if (clientUid == null) clientUid = UUID.randomUUID().toString();
		ShopSaleRequest request = new ShopSaleRequest();
		request.carwash = washer.getId();
		request.clientUid = clientUid;
		request.lines = new ArrayList<>();
		for (String id : cartIds) request.lines.add(new ShopSaleRequest.Line(id, cart.get(id)));
		request.priceDetails = parts;
		paymentDialog.setBusy(true);
		awaitingSale = true;
		BA.getEventBus().post(new AdminShopSaleCreateRequestEvent(request));
	}

	@Subscribe
	public void onSaleCreated(AdminShopSaleCreateResponseEvent event) {
		if (!awaitingSale) return;
		awaitingSale = false;
		if (event.getResult() != null && BaseAssist.isSuccess(event.getResult()) && event.getResult().getResponse() != null) {
			ShopSale sale = event.getResult().getResponse();
			if (paymentDialog != null) paymentDialog.dismiss();
			paymentDialog = null;
			ToastUtil.display(getActivity(), BA.str(R.string.shop_sale_done, sale.getNumber(), Shop.money(sale.getTotal())));
			cart.clear();
			clientUid = null;
			if (sheetBehavior != null) sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
			updateCart();
			loadItems();
			return;
		}
		// The window stays open with the reason; the same client_uid is reused on a retry.
		if (paymentDialog != null) {
			paymentDialog.setBusy(false);
			paymentDialog.showError(Shop.error(event.getResult()));
			lastSaleError = Shop.error(event.getResult());
		}
		// e.g. "Not enough stock": show what is left now.
		if (event.getResult() != null) loadItems();
	}

	private int dp(int value) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
	}

	// ---- adapters ----

	class TileAdapter extends RecyclerView.Adapter<TileAdapter.Holder> {

		@NonNull
		@Override
		public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop_tile, parent, false));
		}

		@Override
		public void onBindViewHolder(@NonNull Holder h, int position) {
			final ShopItem item = shown.get(position);
			h.initial.setText(Shop.initial(item.getName()));
			GradientDrawable circle = new GradientDrawable();
			circle.setShape(GradientDrawable.OVAL);
			circle.setColor(Shop.colorFor(item.getName()));
			h.initial.setBackground(circle);
			h.name.setText(item.getName());
			h.price.setText(Shop.money(item.getPrice()));
			if (item.isTrackStock()) {
				h.left.setVisibility(View.VISIBLE);
				h.left.setText(BA.str(R.string.shop_left, Math.max(0, item.getStock())));
			} else {
				h.left.setVisibility(View.GONE);
			}
			boolean out = item.isOutOfStock();
			h.badge.setVisibility(!out && item.isLow() ? View.VISIBLE : View.GONE);
			Integer inCart = cart.get(item.getId());
			h.inCart.setVisibility(inCart != null && inCart > 0 ? View.VISIBLE : View.GONE);
			h.inCart.setText(inCart != null ? String.valueOf(inCart) : "");
			h.itemView.setAlpha(out ? 0.4f : 1f);
			h.itemView.setEnabled(!out);
			h.itemView.setOnClickListener(out ? null : v -> add(item, 1));
			h.itemView.setOnLongClickListener(out ? null : v -> {
				askQuantity(item);
				return true;
			});
			h.itemView.setClickable(!out);
			h.itemView.setLongClickable(!out);
		}

		@Override
		public int getItemCount() {
			return shown.size();
		}

		class Holder extends RecyclerView.ViewHolder {
			final TextView initial, name, price, left, badge, inCart;

			Holder(View view) {
				super(view);
				initial = view.findViewById(R.id.initial);
				name = view.findViewById(R.id.name);
				price = view.findViewById(R.id.price);
				left = view.findViewById(R.id.left);
				badge = view.findViewById(R.id.badge);
				inCart = view.findViewById(R.id.inCart);
			}
		}
	}

	class CartAdapter extends RecyclerView.Adapter<CartAdapter.Holder> {

		@NonNull
		@Override
		public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop_cart_line, parent, false));
		}

		@Override
		public void onBindViewHolder(@NonNull Holder h, int position) {
			final ShopItem item = itemsById.get(cartIds.get(position));
			if (item == null) return;
			int qty = cart.containsKey(item.getId()) ? cart.get(item.getId()) : 0;
			h.name.setText(item.getName());
			h.sum.setText(Shop.money(item.getPrice()) + " × " + qty + " = " + Shop.money(item.getPrice() * qty));
			h.qty.setText(String.valueOf(qty));
			h.minus.setOnClickListener(v -> add(item, -1));
			h.plus.setOnClickListener(v -> add(item, 1));
			h.plus.setAlpha(qty >= maxQty(item) ? 0.4f : 1f);
			h.qty.setOnClickListener(v -> askQuantity(item));
			h.remove.setOnClickListener(v -> setQty(item, 0));
		}

		@Override
		public int getItemCount() {
			return cartIds.size();
		}

		class Holder extends RecyclerView.ViewHolder {
			final TextView name, sum, qty, minus, plus;
			final View remove;

			Holder(View view) {
				super(view);
				name = view.findViewById(R.id.name);
				sum = view.findViewById(R.id.sum);
				qty = view.findViewById(R.id.qty);
				minus = view.findViewById(R.id.minus);
				plus = view.findViewById(R.id.plus);
				remove = view.findViewById(R.id.remove);
			}
		}
	}
}
