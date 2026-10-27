package com.driverspa.admin.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.activity.AdminShopItemActivity;
import com.driverspa.admin.shop.Shop;
import com.driverspa.assist.BaseAssist;
import com.driverspa.assist.ShopAssist;
import com.driverspa.model.ShopItem;
import com.driverspa.model.ShopStockMove;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.model.api.request.ShopItemRequest;
import com.driverspa.model.api.request.ShopStockRequest;
import com.driverspa.util.Constants;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.ws.AdminShopItemDeleteRequestEvent;
import com.driverspa.util.otto.ws.AdminShopItemDeleteResponseEvent;
import com.driverspa.util.otto.ws.AdminShopItemSaveRequestEvent;
import com.driverspa.util.otto.ws.AdminShopItemSaveResponseEvent;
import com.driverspa.util.otto.ws.AdminShopItemsRequestEvent;
import com.driverspa.util.otto.ws.AdminShopItemsResponseEvent;
import com.driverspa.util.otto.ws.AdminShopMovesRequestEvent;
import com.driverspa.util.otto.ws.AdminShopMovesResponseEvent;
import com.driverspa.util.otto.ws.AdminShopStockRequestEvent;
import com.driverspa.util.otto.ws.AdminShopStockResponseEvent;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.driverspa.util.Converters;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Товары (docs/shop.md, owner and directors): the catalog grouped by category, with the stock,
 * the low-stock colour and the average cost; hidden items under the "Скрытые" toggle. A row
 * opens its actions: Приход, Списать, Изменить, История, Скрыть/Показать, Удалить.
 */
public class ShopItemsFragment extends AdminBaseFragment {

	@BindView(R.id.addItem)
	Button addItem;
	@BindView(R.id.showHidden)
	SwitchCompat showHidden;
	@BindView(R.id.list)
	RecyclerView list;
	@BindView(R.id.empty)
	TextView empty;
	@BindView(R.id.progress)
	View progress;

	private Unbinder unbinder;
	private Washer washer;
	private User user;
	private final List<ShopItem> items = new ArrayList<>();
	/** Rows: a String is a category header, a ShopItem is an item. */
	private final List<Object> rows = new ArrayList<>();
	private final RowsAdapter adapter = new RowsAdapter();
	private boolean loaded;

	/** What this screen is waiting for; responses to other screens' requests are ignored. */
	private boolean awaitingSave, awaitingDelete, awaitingStock;
	private String awaitingMovesFor;

	private AlertDialog stockDialog;
	private TextView stockError;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_shop_items, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		unbinder = ButterKnife.bind(this, view);
		list.setLayoutManager(new LinearLayoutManager(getActivity()));
		list.setAdapter(adapter);
		showHidden.setOnCheckedChangeListener((b, checked) -> buildRows());
		addItem.setOnClickListener(v -> openForm(null));
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
		// Answers that arrive while paused are missed; the list is reloaded on resume.
		if (awaitingStock) unlockStockDialog(); // confirming again resends the same client_uid
		awaitingSave = awaitingDelete = awaitingStock = false;
		awaitingMovesFor = null;
		setWaitScreen(false);
	}

	private ProgressDialog progressDialog;

	/** A wait screen that Back can't close, so a write can't be sent again while in flight. */
	@Override
	void setWaitScreen(boolean set) {
		if (getActivity() == null) return;
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.setTitle("");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.setMessage(BA.str(R.string.loading_dots));
		}
		if (set) progressDialog.show();
		else if (progressDialog.isShowing()) progressDialog.dismiss();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (stockDialog != null && stockDialog.isShowing()) stockDialog.dismiss();
		stockDialog = null;
		if (unbinder != null) unbinder.unbind();
		unbinder = null;
	}

	@Subscribe
	public void onUserReceived(UserGetSelfResponseEvent event) {
		user = event.getUser();
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		load();
	}

	private void load() {
		if (washer == null || unbinder == null) return;
		if (!loaded) progress.setVisibility(View.VISIBLE);
		BA.getEventBus().post(new AdminShopItemsRequestEvent(washer.getId()));
	}

	@Subscribe
	public void onItemsReceived(AdminShopItemsResponseEvent event) {
		if (unbinder == null) return;
		progress.setVisibility(View.GONE);
		if (event.getResult() == null) return;
		if (!BaseAssist.isSuccess(event.getResult()) || event.getResult().getResponse() == null) {
			ToastUtil.display(getActivity(), Shop.error(event.getResult()));
			return;
		}
		loaded = true;
		items.clear();
		if (event.getResult().getResponse().getObjects() != null) {
			for (ShopItem item : event.getResult().getResponse().getObjects()) {
				if (item != null && item.getId() != null) items.add(item);
			}
		}
		buildRows();
	}

	/** Groups the items by category (the backend's order: by category, uncategorized last). */
	private void buildRows() {
		if (unbinder == null) return;
		boolean hidden = showHidden.isChecked();
		LinkedHashMap<String, List<ShopItem>> groups = new LinkedHashMap<>();
		List<ShopItem> none = new ArrayList<>();
		for (ShopItem item : items) {
			if (!hidden && !item.isActive()) continue;
			String c = item.getCategory();
			if (c == null) {
				none.add(item);
				continue;
			}
			List<ShopItem> group = groups.get(c);
			if (group == null) groups.put(c, group = new ArrayList<>());
			group.add(item);
		}
		rows.clear();
		for (Map.Entry<String, List<ShopItem>> e : groups.entrySet()) {
			rows.add(e.getKey());
			rows.addAll(e.getValue());
		}
		if (!none.isEmpty()) {
			if (!groups.isEmpty()) rows.add(BA.str(R.string.shop_no_category));
			rows.addAll(none);
		}
		adapter.notifyDataSetChanged();
		empty.setVisibility(loaded && rows.isEmpty() ? View.VISIBLE : View.GONE);
	}

	private ArrayList<String> categories() {
		ArrayList<String> result = new ArrayList<>();
		for (ShopItem item : items) {
			String c = item.getCategory();
			if (c != null && !result.contains(c)) result.add(c);
		}
		return result;
	}

	// ---- actions ----

	private void openForm(ShopItem item) {
		if (washer == null) return;
		Intent intent = new Intent(getActivity(), AdminShopItemActivity.class);
		intent.putExtra(AdminShopItemActivity.EXTRA_WASHER_ID, washer.getId());
		intent.putStringArrayListExtra(AdminShopItemActivity.EXTRA_CATEGORIES, categories());
		if (item != null) intent.putExtra(AdminShopItemActivity.EXTRA_ITEM, Converters.gsonWithDate().toJson(item));
		startActivity(intent);
	}

	private void openActions(final ShopItem item) {
		final List<Integer> actions = new ArrayList<>();
		if (item.isTrackStock()) actions.add(R.string.shop_action_purchase);
		if (item.isTrackStock() && item.getStock() > 0) actions.add(R.string.shop_action_writeoff);
		actions.add(R.string.shop_action_edit);
		actions.add(R.string.shop_action_history);
		actions.add(item.isActive() ? R.string.shop_action_hide : R.string.shop_action_show);
		actions.add(R.string.shop_action_delete);
		String[] titles = new String[actions.size()];
		for (int i = 0; i < titles.length; i++) titles[i] = BA.str(actions.get(i));

		new MaterialAlertDialogBuilder(requireActivity())
				.setTitle(item.getName())
				.setItems(titles, (d, which) -> {
					int action = actions.get(which);
					if (action == R.string.shop_action_purchase) openStock(item, true);
					else if (action == R.string.shop_action_writeoff) openStock(item, false);
					else if (action == R.string.shop_action_edit) openForm(item);
					else if (action == R.string.shop_action_history) loadMoves(item);
					else if (action == R.string.shop_action_delete) confirmDelete(item);
					else setActive(item, !item.isActive());
				})
				.show();
	}

	private void setActive(ShopItem item, boolean active) {
		if (awaitingSave) return;
		ShopItemRequest request = new ShopItemRequest();
		request.isActive = active;
		awaitingSave = true;
		setWaitScreen(true);
		BA.getEventBus().post(new AdminShopItemSaveRequestEvent(item.getId(), request));
	}

	@Subscribe
	public void onItemSaved(AdminShopItemSaveResponseEvent event) {
		if (!awaitingSave) return;
		awaitingSave = false;
		setWaitScreen(false);
		if (event.getResult() == null) return;
		if (!BaseAssist.isSuccess(event.getResult())) {
			ToastUtil.display(getActivity(), Shop.error(event.getResult()));
			return;
		}
		load();
	}

	private void confirmDelete(final ShopItem item) {
		new MaterialAlertDialogBuilder(requireActivity())
				.setTitle(BA.str(R.string.shop_delete_q, item.getName()))
				.setNeutralButton(BA.str(R.string.no_word), null)
				.setPositiveButton(BA.str(R.string.yes_word), (d, which) -> {
					if (awaitingDelete) return;
					awaitingDelete = true;
					setWaitScreen(true);
					BA.getEventBus().post(new AdminShopItemDeleteRequestEvent(item.getId()));
				})
				.show();
	}

	@Subscribe
	public void onItemDeleted(AdminShopItemDeleteResponseEvent event) {
		if (!awaitingDelete) return;
		awaitingDelete = false;
		setWaitScreen(false);
		if (event.getResult() == null) return;
		if (!BaseAssist.isSuccess(event.getResult())) {
			ToastUtil.display(getActivity(), Shop.error(event.getResult()));
			return;
		}
		ToastUtil.display(getActivity(), BA.str(R.string.shop_item_deleted));
		load();
	}

	/** Приход (purchase) or Списать (write-off). */
	private void openStock(final ShopItem item, final boolean purchase) {
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_shop_stock, null);
		final TextView info = view.findViewById(R.id.stockInfo);
		final EditText qty = view.findViewById(R.id.stockQty);
		final EditText unitCost = view.findViewById(R.id.stockUnitCost);
		final EditText comment = view.findViewById(R.id.stockComment);
		final TextView summary = view.findViewById(R.id.stockSummary);
		final LinearLayout paidBy = view.findViewById(R.id.stockPaidBy);
		stockError = view.findViewById(R.id.stockError);
		view.findViewById(R.id.purchaseLayout).setVisibility(purchase ? View.VISIBLE : View.GONE);
		((TextView) view.findViewById(R.id.stockCommentLabel)).setText(BA.str(purchase ? R.string.shop_comment : R.string.shop_reason));

		String infoText = BA.str(R.string.shop_in_stock, item.getStock());
		if (item.getCost() != null) infoText += " · " + BA.str(R.string.shop_avg_cost, Shop.money(item.getCost()));
		info.setText(infoText);

		final String[] payment = {Constants.PAYMENT_CASH};
		// The client_uid of this attempt: kept for a resend after a timeout, renewed when the
		// quantity, price, payment or comment changes (and dropped with the window on success).
		final String[] uid = {null};
		if (purchase) {
			addPaidByChips(paidBy, payment, () -> uid[0] = null);
			if (item.getCost() != null && item.getCost() > 0) unitCost.setText(plain(item.getCost()));
		}

		final Runnable updateSummary = () -> {
			long q = Shop.number(qty);
			if (q <= 0) {
				summary.setText("");
				return;
			}
			if (purchase) {
				double unit = Shop.decimal(unitCost);
				if (unit < 0) {
					summary.setText("");
					return;
				}
				String text = BA.str(R.string.shop_purchase_total, Shop.money(Math.round(q * unit * 100) / 100.0));
				if (item.getCost() != null) {
					double stock = Math.max(item.getStock(), 0);
					double avg = (stock * item.getCost() + q * unit) / (stock + q);
					text += "\n" + BA.str(R.string.shop_new_avg_cost, Shop.money(Math.round(avg * 100) / 100.0));
				}
				summary.setText(text);
			} else if (item.getCost() != null) {
				summary.setText(BA.str(R.string.shop_writeoff_cost, Shop.money(Math.round(q * item.getCost() * 100) / 100.0)));
			}
		};
		TextWatcher watcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void afterTextChanged(Editable s) {
				uid[0] = null;
				updateSummary.run();
			}
		};
		qty.addTextChangedListener(watcher);
		unitCost.addTextChangedListener(watcher);
		comment.addTextChangedListener(watcher);

		stockDialog = new MaterialAlertDialogBuilder(requireActivity())
				.setTitle(BA.str(purchase ? R.string.shop_purchase_title : R.string.shop_writeoff_title, item.getName()))
				.setView(view)
				.setNegativeButton(BA.str(R.string.cancel_word), null)
				.setPositiveButton(BA.str(purchase ? R.string.shop_action_purchase : R.string.shop_action_writeoff), null)
				.create();
		stockDialog.setOnShowListener(d -> stockDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
			if (awaitingStock) return;
			long q = Shop.number(qty);
			if (q <= 0 || q > Shop.MAX_QTY) {
				qty.setError(BA.str(q <= 0 ? R.string.shop_qty_invalid : R.string.shop_err_qty));
				return;
			}
			ShopStockRequest request = new ShopStockRequest();
			request.qty = (int) q;
			String text = comment.getText().toString().trim();
			if (purchase) {
				double unit = Shop.decimal(unitCost);
				if (unit < 0 || unit > Shop.MAX_MONEY) {
					unitCost.setError(BA.str(unit < 0 ? R.string.shop_unit_cost_required : R.string.shop_err_unit_cost));
					return;
				}
				request.unitCost = unit;
				request.paymentType = payment[0];
				if (!text.isEmpty()) request.comment = text;
			} else {
				if (q > item.getStock()) {
					qty.setError(BA.str(R.string.shop_qty_max, Math.max(0, item.getStock())));
					return;
				}
				if (text.isEmpty()) {
					comment.setError(BA.str(R.string.shop_reason_required));
					return;
				}
				request.comment = text;
			}
			if (uid[0] == null) uid[0] = java.util.UUID.randomUUID().toString();
			request.clientUid = uid[0];
			stockError.setVisibility(View.GONE);
			awaitingStock = true;
			v.setEnabled(false);
			stockDialog.setCancelable(false);
			setWaitScreen(true);
			BA.getEventBus().post(new AdminShopStockRequestEvent(item.getId(), purchase, request));
		}));
		stockDialog.show();
	}

	private void addPaidByChips(LinearLayout container, final String[] payment, final Runnable onChange) {
		String[] types = {Constants.PAYMENT_CASH, Constants.PAYMENT_CARD, Shop.PAY_TRANSFER};
		int[] titles = {R.string.shop_paid_cash, R.string.shop_paid_card, R.string.shop_paid_transfer};
		for (int i = 0; i < types.length; i++) {
			final String type = types[i];
			TextView chip = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.item_shop_chip, container, false);
			chip.setText(BA.str(titles[i]));
			chip.setSelected(type.equals(payment[0]));
			chip.setOnClickListener(v -> {
				if (!type.equals(payment[0])) onChange.run();
				payment[0] = type;
				for (int j = 0; j < container.getChildCount(); j++) container.getChildAt(j).setSelected(false);
				v.setSelected(true);
			});
			container.addView(chip);
		}
	}

	@Subscribe
	public void onStockSaved(AdminShopStockResponseEvent event) {
		if (!awaitingStock) return;
		awaitingStock = false;
		setWaitScreen(false);
		unlockStockDialog();
		if (event.getResult() == null) {
			// No answer: confirming again resends the same client_uid, which isn't applied twice.
			if (stockError != null && stockDialog != null && stockDialog.isShowing()) {
				stockError.setText(BA.str(R.string.connection_error));
				stockError.setVisibility(View.VISIBLE);
			}
			return;
		}
		if (!BaseAssist.isSuccess(event.getResult())) {
			// The dialog stays open with the reason.
			if (stockDialog != null && stockDialog.isShowing() && stockError != null) {
				stockError.setText(Shop.error(event.getResult()));
				stockError.setVisibility(View.VISIBLE);
			} else {
				ToastUtil.display(getActivity(), Shop.error(event.getResult()));
			}
			return;
		}
		if (stockDialog != null && stockDialog.isShowing()) stockDialog.dismiss();
		stockDialog = null;
		ToastUtil.display(getActivity(), BA.str(event.getPurchase() ? R.string.shop_purchase_done : R.string.shop_writeoff_done));
		load();
	}

	private void unlockStockDialog() {
		if (stockDialog != null && stockDialog.isShowing()) {
			stockDialog.setCancelable(true);
			stockDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
		}
	}

	// ---- history ----

	/** The moves of the item whose history is open, newest first, as loaded so far. */
	private final List<ShopStockMove> moves = new ArrayList<>();
	private String movesItem;
	private int awaitingMovesOffset;

	private void loadMoves(ShopItem item) {
		moves.clear();
		movesItem = item.getId();
		requestMoves(0);
	}

	private void requestMoves(int offset) {
		if (movesItem == null || awaitingMovesFor != null) return;
		awaitingMovesFor = movesItem;
		awaitingMovesOffset = offset;
		setWaitScreen(true);
		BA.getEventBus().post(new AdminShopMovesRequestEvent(movesItem).setOffset(offset));
	}

	@Subscribe
	public void onMovesReceived(AdminShopMovesResponseEvent event) {
		if (awaitingMovesFor == null || !awaitingMovesFor.equals(event.getItemId())
				|| event.getOffset() != awaitingMovesOffset) return;
		awaitingMovesFor = null;
		setWaitScreen(false);
		if (event.getResult() == null || getActivity() == null) return;
		if (!BaseAssist.isSuccess(event.getResult()) || event.getResult().getResponse() == null) {
			ToastUtil.display(getActivity(), Shop.error(event.getResult()));
			return;
		}
		String name = "";
		for (ShopItem item : items) if (item.getId().equals(event.getItemId())) name = item.getName();
		List<ShopStockMove> page = event.getResult().getResponse().getObjects();
		if (event.getOffset() == 0) moves.clear();
		if (page != null) moves.addAll(page);
		long total = event.getResult().getResponse().getTotalCount();
		boolean more = page != null && !page.isEmpty()
				&& (total >= 0 ? moves.size() < total : page.size() >= ShopAssist.MOVES_LIMIT);
		StringBuilder text = new StringBuilder();
		if (moves == null || moves.isEmpty()) {
			text.append(BA.str(R.string.shop_history_empty));
		} else {
			for (ShopStockMove m : moves) {
				if (text.length() > 0) text.append("\n\n");
				text.append(Shop.dateTime(m.getTs())).append(" · ").append(moveKind(m));
				text.append("  ").append(m.getQty() > 0 ? "+" : "").append(m.getQty());
				if (m.getUnitCost() != null && ShopStockMove.PURCHASE.equals(m.getKind())) {
					text.append(" × ").append(Shop.money(m.getUnitCost()));
				}
				if (!TextUtils.isEmpty(m.getPaymentType())) text.append(" · ").append(Shop.paymentName(m.getPaymentType()));
				if (m.getSaleNumber() != null) text.append(" · ").append(BA.str(R.string.shop_sale_no, m.getSaleNumber()));
				if (!TextUtils.isEmpty(m.getUserName())) text.append("\n").append(m.getUserName());
				if (!TextUtils.isEmpty(m.getComment())) text.append("\n").append(m.getComment());
			}
		}
		AlertDialog.Builder builder = new MaterialAlertDialogBuilder(requireActivity())
				.setTitle(BA.str(R.string.shop_history_title, name))
				.setMessage(text.toString())
				.setPositiveButton(BA.str(R.string.shop_close), null);
		// The next page: the dialog is shown again with all the moves loaded so far.
		if (more) builder.setNeutralButton(BA.str(R.string.shop_load_more), (d, which) -> requestMoves(moves.size()));
		builder.show();
	}

	private static String moveKind(ShopStockMove m) {
		String kind = m.getKind();
		if (ShopStockMove.PURCHASE.equals(kind)) return BA.str(R.string.shop_move_purchase);
		if (ShopStockMove.SALE.equals(kind)) return BA.str(R.string.shop_move_sale);
		if (ShopStockMove.WRITEOFF.equals(kind)) return BA.str(R.string.shop_move_writeoff);
		if (ShopStockMove.CANCEL.equals(kind)) return BA.str(R.string.shop_move_cancel);
		return kind != null ? kind : "";
	}

	/** A cost for an input field: no grouping, no trailing ".0". */
	private static String plain(double value) {
		if (value == Math.rint(value)) return String.valueOf((long) value);
		return String.valueOf(Math.round(value * 100) / 100.0);
	}

	// ---- the list ----

	class RowsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

		private static final int HEADER = 0;
		private static final int ITEM = 1;

		@Override
		public int getItemViewType(int position) {
			return rows.get(position) instanceof String ? HEADER : ITEM;
		}

		@NonNull
		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			if (viewType == HEADER) {
				return new RecyclerView.ViewHolder(inflater.inflate(R.layout.item_shop_header, parent, false)) {
				};
			}
			return new ItemHolder(inflater.inflate(R.layout.item_shop_item_row, parent, false));
		}

		@Override
		public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
			Object row = rows.get(position);
			if (row instanceof String) {
				((TextView) holder.itemView).setText((String) row);
				return;
			}
			final ShopItem item = (ShopItem) row;
			ItemHolder h = (ItemHolder) holder;
			h.name.setText(item.isActive() ? item.getName()
					: item.getName() + " (" + BA.str(R.string.shop_hidden_mark) + ")");
			h.price.setText(Shop.money(item.getPrice()));
			int color;
			if (!item.isTrackStock()) {
				h.stock.setText(BA.str(R.string.shop_unlimited));
				color = R.color.BlueLight;
			} else {
				h.stock.setText(BA.str(R.string.shop_stock_n, item.getStock()));
				color = item.getStock() <= 0 ? R.color.OrangeRed : item.isLow() ? R.color.Orange : R.color.BlueLight2;
			}
			h.stock.setTextColor(getResources().getColor(color));
			boolean showCost = item.isTrackStock() && item.getCost() != null;
			h.cost.setVisibility(showCost ? View.VISIBLE : View.GONE);
			if (showCost) h.cost.setText(BA.str(R.string.shop_avg_cost, Shop.money(item.getCost())));
			h.itemView.setAlpha(item.isActive() ? 1f : 0.5f);
			h.itemView.setOnClickListener(v -> openActions(item));
		}

		@Override
		public int getItemCount() {
			return rows.size();
		}

		class ItemHolder extends RecyclerView.ViewHolder {
			final TextView name, price, stock, cost;

			ItemHolder(View view) {
				super(view);
				name = view.findViewById(R.id.name);
				price = view.findViewById(R.id.price);
				stock = view.findViewById(R.id.stock);
				cost = view.findViewById(R.id.cost);
			}
		}
	}
}
