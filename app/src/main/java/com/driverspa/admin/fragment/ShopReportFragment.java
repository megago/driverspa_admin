package com.driverspa.admin.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.shop.Shop;
import com.driverspa.assist.BaseAssist;
import com.driverspa.fragment.DatePickerFragment;
import com.driverspa.model.ShopReport;
import com.driverspa.model.Washer;
import com.driverspa.util.Functions;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.ws.AdminShopReportRequestEvent;
import com.driverspa.util.otto.ws.AdminShopReportResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.squareup.otto.Subscribe;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Отчёт (docs/shop.md, owner and directors): a period (today, week, month or custom), the cards
 * (revenue, cost, gross profit, number of sales, average sale), sales per item (sortable by
 * tapping a column) and the items running low. Cost and profit are left out for operators.
 */
public class ShopReportFragment extends AdminBaseFragment {

	private static final String STATE_PERIOD = "STATE_PERIOD";
	private static final String STATE_FROM = "STATE_FROM";
	private static final String STATE_TO = "STATE_TO";
	private static final String STATE_SORT = "STATE_SORT";
	private static final String STATE_SORT_ASC = "STATE_SORT_ASC";

	private static final int PERIOD_TODAY = 0;
	private static final int PERIOD_WEEK = 1;
	private static final int PERIOD_MONTH = 2;
	private static final int PERIOD_CUSTOM = 3;

	private static final int SORT_NAME = 0;
	private static final int SORT_QTY = 1;
	private static final int SORT_REVENUE = 2;
	private static final int SORT_COST = 3;
	private static final int SORT_PROFIT = 4;

	@BindView(R.id.periodToday)
	TextView periodToday;
	@BindView(R.id.periodWeek)
	TextView periodWeek;
	@BindView(R.id.periodMonth)
	TextView periodMonth;
	@BindView(R.id.periodCustom)
	TextView periodCustom;
	@BindView(R.id.fromDate)
	TextView fromDate;
	@BindView(R.id.toDate)
	TextView toDate;
	@BindView(R.id.progress)
	View progress;
	@BindView(R.id.revenue)
	TextView revenue;
	@BindView(R.id.cost)
	TextView cost;
	@BindView(R.id.costCard)
	View costCard;
	@BindView(R.id.profit)
	TextView profit;
	@BindView(R.id.profitCard)
	View profitCard;
	@BindView(R.id.salesCount)
	TextView salesCount;
	@BindView(R.id.average)
	TextView average;
	@BindView(R.id.reportEmpty)
	View reportEmpty;
	@BindView(R.id.itemsTable)
	View itemsTable;
	@BindView(R.id.itemsRows)
	LinearLayout itemsRows;
	@BindView(R.id.lowRows)
	LinearLayout lowRows;
	@BindView(R.id.colName)
	TextView colName;
	@BindView(R.id.colQty)
	TextView colQty;
	@BindView(R.id.colRevenue)
	TextView colRevenue;
	@BindView(R.id.colCost)
	TextView colCost;
	@BindView(R.id.colProfit)
	TextView colProfit;

	private Unbinder unbinder;
	private Washer washer;
	private int period = PERIOD_TODAY;
	/** The custom period: first and last day, as picked. */
	private LocalDate customFrom, customTo;
	private int sort = SORT_REVENUE;
	private boolean sortAscending;
	private ShopReport report;
	/** The number of the latest request; answers to older ones (Неделя then Месяц) are dropped. */
	private int requestSeq;
	private boolean awaiting;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_shop_report, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		unbinder = ButterKnife.bind(this, view);
		LocalDate today = LocalDate.now(zone());
		customFrom = today;
		customTo = today;
		if (savedInstanceState != null) {
			period = savedInstanceState.getInt(STATE_PERIOD, PERIOD_TODAY);
			customFrom = LocalDate.ofEpochDay(savedInstanceState.getLong(STATE_FROM, today.toEpochDay()));
			customTo = LocalDate.ofEpochDay(savedInstanceState.getLong(STATE_TO, today.toEpochDay()));
			sort = savedInstanceState.getInt(STATE_SORT, SORT_REVENUE);
			sortAscending = savedInstanceState.getBoolean(STATE_SORT_ASC, false);
		}
		periodToday.setOnClickListener(v -> setPeriod(PERIOD_TODAY));
		periodWeek.setOnClickListener(v -> setPeriod(PERIOD_WEEK));
		periodMonth.setOnClickListener(v -> setPeriod(PERIOD_MONTH));
		periodCustom.setOnClickListener(v -> setPeriod(PERIOD_CUSTOM));
		fromDate.setOnClickListener(v -> pickDate(true));
		toDate.setOnClickListener(v -> pickDate(false));
		colName.setOnClickListener(v -> sortBy(SORT_NAME));
		colQty.setOnClickListener(v -> sortBy(SORT_QTY));
		colRevenue.setOnClickListener(v -> sortBy(SORT_REVENUE));
		colCost.setOnClickListener(v -> sortBy(SORT_COST));
		colProfit.setOnClickListener(v -> sortBy(SORT_PROFIT));
		updatePeriodViews();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_PERIOD, period);
		if (customFrom != null) outState.putLong(STATE_FROM, customFrom.toEpochDay());
		if (customTo != null) outState.putLong(STATE_TO, customTo.toEpochDay());
		outState.putInt(STATE_SORT, sort);
		outState.putBoolean(STATE_SORT_ASC, sortAscending);
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
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		load();
	}

	/** Days are counted in the car wash's zone, as the backend counts its days. */
	private static ZoneId zone() {
		return Functions.getCarwashZone();
	}

	private void setPeriod(int value) {
		period = value;
		load();
	}

	private LocalDate firstDay() {
		LocalDate today = LocalDate.now(zone());
		switch (period) {
			case PERIOD_WEEK:
				return today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
			case PERIOD_MONTH:
				return today.withDayOfMonth(1);
			case PERIOD_CUSTOM:
				return customFrom;
			default:
				return today;
		}
	}

	private LocalDate lastDay() {
		return period == PERIOD_CUSTOM ? customTo : LocalDate.now(zone());
	}

	private void updatePeriodViews() {
		if (unbinder == null) return;
		periodToday.setSelected(period == PERIOD_TODAY);
		periodWeek.setSelected(period == PERIOD_WEEK);
		periodMonth.setSelected(period == PERIOD_MONTH);
		periodCustom.setSelected(period == PERIOD_CUSTOM);
		DateTimeFormatter f = DateTimeFormatter.ofPattern("dd.MM.yyyy");
		fromDate.setText(f.format(firstDay()));
		toDate.setText(f.format(lastDay()));
		fromDate.setEnabled(period == PERIOD_CUSTOM);
		toDate.setEnabled(period == PERIOD_CUSTOM);
	}

	private void pickDate(final boolean from) {
		if (period != PERIOD_CUSTOM) return;
		LocalDate current = from ? customFrom : customTo;
		// The picker works in UTC days: 00:00 UTC of the shown day.
		long initial = current.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli();
		DatePickerFragment picker = DatePickerFragment.newInstance(from ? "start" : "end", initial);
		picker.setDatePickerDateSetListener((time, type) -> {
			if (time == null) return;
			LocalDate day = Instant.ofEpochMilli(time).atZone(java.time.ZoneOffset.UTC).toLocalDate();
			if (from) {
				if (day.isAfter(customTo)) {
					ToastUtil.display(getActivity(), BA.str(R.string.err_start_date_after_end));
					return;
				}
				customFrom = day;
			} else {
				if (day.isBefore(customFrom)) {
					ToastUtil.display(getActivity(), BA.str(R.string.err_end_date_before_start));
					return;
				}
				customTo = day;
			}
			load();
		});
		picker.show(getChildFragmentManager(), "shopReportDate");
	}

	private void load() {
		if (washer == null || unbinder == null) return;
		updatePeriodViews();
		Instant start = firstDay().atStartOfDay(zone()).toInstant();
		Instant end = lastDay().plusDays(1).atStartOfDay(zone()).toInstant().minusSeconds(1);
		progress.setVisibility(View.VISIBLE);
		awaiting = true;
		BA.getEventBus().post(new AdminShopReportRequestEvent(washer.getId(), start.getEpochSecond(), end.getEpochSecond())
				.setSeq(++requestSeq));
	}

	@Subscribe
	public void onReportReceived(AdminShopReportResponseEvent event) {
		if (!awaiting || unbinder == null || event.getSeq() != requestSeq) return;
		awaiting = false;
		progress.setVisibility(View.GONE);
		if (event.getResult() == null) return;
		if (!BaseAssist.isSuccess(event.getResult()) || event.getResult().getResponse() == null) {
			ToastUtil.display(getActivity(), Shop.error(event.getResult()));
			return;
		}
		report = event.getResult().getResponse();
		showReport();
	}

	private void showReport() {
		if (report == null || unbinder == null) return;
		ShopReport.Totals totals = report.getTotals();
		revenue.setText(Shop.money(totals != null ? totals.getRevenue() : 0));
		boolean showCost = totals != null && totals.getCost() != null;
		costCard.setVisibility(showCost ? View.VISIBLE : View.INVISIBLE);
		profitCard.setVisibility(totals != null && totals.getProfit() != null ? View.VISIBLE : View.INVISIBLE);
		cost.setText(showCost ? Shop.money(totals.getCost()) : "");
		profit.setText(totals != null && totals.getProfit() != null ? Shop.money(totals.getProfit()) : "");
		salesCount.setText(String.valueOf(totals != null ? totals.getSales() : 0));
		average.setText(Shop.money(totals != null ? Math.round(totals.getAverage()) : 0));

		List<ShopReport.Entry> entries = report.getItems() != null ? new ArrayList<>(report.getItems()) : new ArrayList<>();
		reportEmpty.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
		itemsTable.setVisibility(entries.isEmpty() ? View.GONE : View.VISIBLE);
		colCost.setVisibility(showCost ? View.VISIBLE : View.GONE);
		colProfit.setVisibility(showCost ? View.VISIBLE : View.GONE);
		sortEntries(entries);
		updateSortMarks();
		itemsRows.removeAllViews();
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		for (ShopReport.Entry e : entries) {
			View row = inflater.inflate(R.layout.item_shop_report_row, itemsRows, false);
			((TextView) row.findViewById(R.id.name)).setText(e.getName());
			((TextView) row.findViewById(R.id.qty)).setText(String.valueOf(e.getQty()));
			((TextView) row.findViewById(R.id.revenue)).setText(Shop.money(e.getRevenue()));
			TextView c = row.findViewById(R.id.cost);
			TextView p = row.findViewById(R.id.profit);
			c.setVisibility(showCost ? View.VISIBLE : View.GONE);
			p.setVisibility(showCost ? View.VISIBLE : View.GONE);
			c.setText(e.getCost() != null ? Shop.money(e.getCost()) : "");
			p.setText(e.getProfit() != null ? Shop.money(e.getProfit()) : "");
			itemsRows.addView(row);
		}

		lowRows.removeAllViews();
		List<ShopReport.Low> low = report.getLowStock();
		if (low == null || low.isEmpty()) {
			lowRows.addView(lowLine(BA.str(R.string.shop_no_low_stock), R.color.BlueLight));
		} else {
			for (ShopReport.Low l : low) {
				lowRows.addView(lowLine(BA.str(R.string.shop_low_stock_line, l.getName(), l.getStock(), l.getLowStock()),
						l.getStock() <= 0 ? R.color.OrangeRed : R.color.Orange));
			}
		}
	}

	private TextView lowLine(String text, int color) {
		TextView view = new TextView(getActivity());
		view.setText(text);
		view.setTextColor(getResources().getColor(color));
		view.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_small));
		view.setPadding(0, 6, 0, 6);
		return view;
	}

	private void sortBy(int column) {
		if (sort == column) sortAscending = !sortAscending;
		else {
			sort = column;
			sortAscending = column == SORT_NAME;
		}
		showReport();
	}

	private void sortEntries(List<ShopReport.Entry> entries) {
		Comparator<ShopReport.Entry> comparator;
		switch (sort) {
			case SORT_NAME:
				comparator = (a, b) -> a.getName().compareToIgnoreCase(b.getName());
				break;
			case SORT_QTY:
				comparator = (a, b) -> Integer.compare(a.getQty(), b.getQty());
				break;
			case SORT_COST:
				comparator = (a, b) -> Double.compare(value(a.getCost()), value(b.getCost()));
				break;
			case SORT_PROFIT:
				comparator = (a, b) -> Double.compare(value(a.getProfit()), value(b.getProfit()));
				break;
			default:
				comparator = (a, b) -> Double.compare(a.getRevenue(), b.getRevenue());
		}
		Collections.sort(entries, sortAscending ? comparator : comparator.reversed());
	}

	private static double value(Double d) {
		return d != null ? d : 0;
	}

	private void updateSortMarks() {
		String mark = sortAscending ? " ▲" : " ▼";
		colName.setText(BA.str(R.string.shop_col_item) + (sort == SORT_NAME ? mark : ""));
		colQty.setText(BA.str(R.string.shop_col_qty) + (sort == SORT_QTY ? mark : ""));
		colRevenue.setText(BA.str(R.string.shop_revenue) + (sort == SORT_REVENUE ? mark : ""));
		colCost.setText(BA.str(R.string.shop_cost) + (sort == SORT_COST ? mark : ""));
		colProfit.setText(BA.str(R.string.shop_profit) + (sort == SORT_PROFIT ? mark : ""));
	}
}
