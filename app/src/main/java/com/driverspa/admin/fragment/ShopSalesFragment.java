package com.driverspa.admin.fragment;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.shop.Shop;
import com.driverspa.admin.shop.ShopSaleDialog;
import com.driverspa.assist.BaseAssist;
import com.driverspa.fragment.DatePickerFragment;
import com.driverspa.model.ShopSale;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.ws.AdminShopSaleCancelResponseEvent;
import com.driverspa.util.otto.ws.AdminShopSalesRequestEvent;
import com.driverspa.util.otto.ws.AdminShopSalesResponseEvent;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.squareup.otto.Subscribe;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Продажи (docs/shop.md): the sales of one day (today or a picked date), newest first, with the
 * day total. Canceled sales are struck through. A sale opens its details; managers can cancel.
 */
public class ShopSalesFragment extends AdminBaseFragment {

	private static final String STATE_DATE = "STATE_DATE";
	private static final String DAY_TODAY = "today";

	@BindView(R.id.dayToday)
	TextView dayToday;
	@BindView(R.id.dayPick)
	TextView dayPick;
	@BindView(R.id.dayTotal)
	TextView dayTotal;
	@BindView(R.id.list)
	RecyclerView list;
	@BindView(R.id.empty)
	TextView empty;
	@BindView(R.id.progress)
	View progress;

	private Unbinder unbinder;
	private Washer washer;
	private User user;
	/** The picked date (00:00 UTC of that day, ms, as the date picker gives it), or -1 for today. */
	private long pickedDate = -1;
	private final List<ShopSale> sales = new ArrayList<>();
	private final SalesAdapter adapter = new SalesAdapter();
	/** The number of the latest request; answers to older ones (a quick day change) are dropped. */
	private int requestSeq;
	private boolean awaiting;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_shop_sales, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		unbinder = ButterKnife.bind(this, view);
		if (savedInstanceState != null) pickedDate = savedInstanceState.getLong(STATE_DATE, -1);
		list.setLayoutManager(new LinearLayoutManager(getActivity()));
		list.setAdapter(adapter);
		dayToday.setOnClickListener(v -> {
			pickedDate = -1;
			load();
		});
		dayPick.setOnClickListener(v -> pickDate());
		updateDayChips();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(STATE_DATE, pickedDate);
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
		awaiting = false;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
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

	private void pickDate() {
		DatePickerFragment picker = DatePickerFragment.newInstance(pickedDate >= 0 ? pickedDate : System.currentTimeMillis());
		// As on the orders screen: the picked day at 00:00 UTC, sent in epoch seconds.
		picker.setDatePickerDateSetListener((time, type) -> {
			if (time == null) return;
			pickedDate = time;
			load();
		});
		picker.show(getChildFragmentManager(), "shopSalesDate");
	}

	private void updateDayChips() {
		if (unbinder == null) return;
		dayToday.setSelected(pickedDate < 0);
		dayPick.setSelected(pickedDate >= 0);
		dayPick.setText(pickedDate >= 0
				? DateTimeFormatter.ofPattern("dd.MM.yyyy").format(Instant.ofEpochMilli(pickedDate).atZone(ZoneOffset.UTC))
				: BA.str(R.string.shop_pick_date));
	}

	private void load() {
		if (washer == null || unbinder == null) return;
		updateDayChips();
		progress.setVisibility(View.VISIBLE);
		awaiting = true;
		int seq = ++requestSeq;
		if (pickedDate >= 0) {
			BA.getEventBus().post(new AdminShopSalesRequestEvent(washer.getId(), Constants.FOR_DATE, pickedDate / 1000).setSeq(seq));
		} else {
			BA.getEventBus().post(new AdminShopSalesRequestEvent(washer.getId(), DAY_TODAY, null).setSeq(seq));
		}
	}

	@Subscribe
	public void onSalesReceived(AdminShopSalesResponseEvent event) {
		if (!awaiting || unbinder == null || event.getSeq() != requestSeq) return;
		awaiting = false;
		progress.setVisibility(View.GONE);
		if (event.getResult() == null) return;
		if (!BaseAssist.isSuccess(event.getResult()) || event.getResult().getResponse() == null) {
			ToastUtil.display(getActivity(), Shop.error(event.getResult()));
			return;
		}
		sales.clear();
		if (event.getResult().getResponse().getObjects() != null) {
			for (ShopSale sale : event.getResult().getResponse().getObjects()) if (sale != null) sales.add(sale);
		}
		adapter.notifyDataSetChanged();
		dayTotal.setText(BA.str(R.string.shop_day_total, Shop.money(event.getResult().getResponse().getRevenue())));
		empty.setVisibility(sales.isEmpty() ? View.VISIBLE : View.GONE);
	}

	@Subscribe
	public void onSaleCanceled(AdminShopSaleCancelResponseEvent event) {
		if (event.getResult() != null && BaseAssist.isSuccess(event.getResult())) load();
	}

	private static String linesShort(ShopSale sale) {
		StringBuilder b = new StringBuilder();
		if (sale.getLines() != null) {
			for (ShopSale.Line line : sale.getLines()) {
				if (b.length() > 0) b.append(", ");
				b.append(line.getName());
				if (line.getQty() != 1) b.append(" ×").append(line.getQty());
			}
		}
		return b.toString();
	}

	class SalesAdapter extends RecyclerView.Adapter<SalesAdapter.Holder> {

		@NonNull
		@Override
		public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop_sale_row, parent, false));
		}

		@Override
		public void onBindViewHolder(@NonNull Holder h, int position) {
			final ShopSale sale = sales.get(position);
			StringBuilder title = new StringBuilder(BA.str(R.string.shop_sale_no, sale.getNumber()));
			title.append(" · ").append(Shop.time(sale.getTs()));
			if (sale.getUserName() != null && !sale.getUserName().isEmpty()) title.append(" · ").append(sale.getUserName());
			h.title.setText(title.toString());
			h.total.setText(Shop.money(sale.getTotal()));
			h.lines.setText(linesShort(sale));
			h.payment.setText(sale.isCanceled() ? BA.str(R.string.shop_status_canceled)
					: Shop.paymentSummary(sale.getPriceDetails()));
			int strike = sale.isCanceled() ? Paint.STRIKE_THRU_TEXT_FLAG : 0;
			for (TextView t : new TextView[]{h.title, h.total, h.lines}) {
				t.setPaintFlags((t.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG) | strike);
			}
			h.itemView.setAlpha(sale.isCanceled() ? 0.6f : 1f);
			h.itemView.setOnClickListener(v -> {
				if (getActivity() != null) ShopSaleDialog.show(getActivity(), sale, Shop.isManager(washer, user));
			});
		}

		@Override
		public int getItemCount() {
			return sales.size();
		}

		class Holder extends RecyclerView.ViewHolder {
			final TextView title, total, lines, payment;

			Holder(View view) {
				super(view);
				title = view.findViewById(R.id.title);
				total = view.findViewById(R.id.total);
				lines = view.findViewById(R.id.lines);
				payment = view.findViewById(R.id.payment);
			}
		}
	}
}
