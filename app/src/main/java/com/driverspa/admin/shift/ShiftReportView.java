package com.driverspa.admin.shift;

import android.content.Context;
import android.widget.LinearLayout;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.shift.ShiftReport;

import java.util.Map;

/**
 * The figures of an X-report (open shift) or a Z-report (closed): revenue by payment way,
 * washes, shop, the drawer, cash moves, expenses, washers and boxes. Cash figures the user may
 * not see (an operator on an open shift) come as null and are left out.
 */
public final class ShiftReportView {

	private ShiftReportView() {
	}

	/** Labels of the payment ways, in ShiftReport.PAYMENT_KEYS order. */
	static int paymentLabel(String key) {
		switch (key) {
			case "CD": return R.string.shift_pay_card;
			case "QR": return R.string.shift_pay_qr;
			case "DA": return R.string.shift_pay_deposit;
			case "B": return R.string.shift_pay_bonus;
			default: return R.string.shift_pay_cash;
		}
	}

	/** Labels of the cash flows, in ShiftReport.FLOW_KEYS order. */
	static int flowLabel(String key) {
		switch (key) {
			case "washes": return R.string.shift_flow_washes;
			case "shop": return R.string.shift_flow_shop;
			case "deposits": return R.string.shift_flow_deposits;
			case "other_income": return R.string.shift_flow_other_income;
			case "cash_in": return R.string.shift_flow_cash_in;
			case "cash_out": return R.string.shift_flow_cash_out;
			case "expenses": return R.string.shift_flow_expenses;
			case "payouts": return R.string.shift_flow_payouts;
			case "purchases": return R.string.shift_flow_purchases;
			case "refunds": return R.string.shift_flow_refunds;
			default: return R.string.shift_flow_other;
		}
	}

	public static void render(Context c, LinearLayout into, ShiftReport r) {
		into.addView(ShiftUi.hint(c, Shifts.dateTime(r.getFrom()) + " — " + Shifts.dateTime(r.getTo())));

		// revenue
		LinearLayout revenue = ShiftUi.card(c);
		revenue.addView(ShiftUi.title(c, BA.str(R.string.shift_revenue)));
		revenue.addView(ShiftUi.row(c, BA.str(R.string.shift_revenue_total), Shifts.money(r.getRevenue().getTotal()), R.color.White, true));
		revenue.addView(ShiftUi.row(c, BA.str(R.string.shift_revenue_washes), Shifts.money(r.getRevenue().getWashes())));
		revenue.addView(ShiftUi.row(c, BA.str(R.string.shift_revenue_shop), Shifts.money(r.getRevenue().getShop())));
		revenue.addView(ShiftUi.divider(c));
		boolean any = false;
		for (String key : ShiftReport.PAYMENT_KEYS) {
			double amount = r.getRevenue().byPayment(key);
			if (amount == 0 && !"C".equals(key)) continue;
			any = true;
			revenue.addView(ShiftUi.row(c, BA.str(paymentLabel(key)), Shifts.money(amount)));
		}
		if (!any) revenue.addView(ShiftUi.row(c, BA.str(R.string.shift_pay_cash), Shifts.money(0)));
		into.addView(revenue);

		// washes
		ShiftReport.Washes w = r.getWashes();
		LinearLayout washes = ShiftUi.card(c);
		washes.addView(ShiftUi.title(c, BA.str(R.string.shift_washes)));
		washes.addView(ShiftUi.row(c, BA.str(R.string.shift_cars), String.valueOf(w.getCars())));
		if (w.getOtherServices() > 0) washes.addView(ShiftUi.row(c, BA.str(R.string.shift_other_services), String.valueOf(w.getOtherServices())));
		washes.addView(ShiftUi.row(c, BA.str(R.string.shift_average), Shifts.money(w.getAverage())));
		washes.addView(ShiftUi.row(c, BA.str(R.string.shift_discounts), Shifts.money(w.getDiscounts())));
		washes.addView(ShiftUi.row(c, BA.str(R.string.shift_cancellations), String.valueOf(r.getCancellations())));
		washes.addView(ShiftUi.row(c, BA.str(R.string.shift_no_shows), String.valueOf(r.getNoShows())));
		into.addView(washes);

		// shop
		ShiftReport.ShopPart shop = r.getShop();
		if (shop.getSales() > 0 || shop.getCanceled() > 0) {
			LinearLayout s = ShiftUi.card(c);
			s.addView(ShiftUi.title(c, BA.str(R.string.shift_shop)));
			s.addView(ShiftUi.row(c, BA.str(R.string.shift_shop_sales), String.valueOf(shop.getSales())));
			s.addView(ShiftUi.row(c, BA.str(R.string.shift_shop_canceled), String.valueOf(shop.getCanceled())));
			s.addView(ShiftUi.row(c, BA.str(R.string.shift_revenue_shop), Shifts.money(shop.getRevenue())));
			into.addView(s);
		}

		// the drawer
		ShiftReport.Cash cash = r.getCash();
		LinearLayout drawer = ShiftUi.card(c);
		drawer.addView(ShiftUi.title(c, BA.str(R.string.shift_drawer)));
		if (cash.getOpening() != null) drawer.addView(ShiftUi.row(c, BA.str(R.string.shift_opening_cash), Shifts.money(cash.getOpening())));
		Map<String, Double> flows = cash.getFlows();
		if (flows != null) {
			for (String key : ShiftReport.FLOW_KEYS) {
				Double v = flows.get(key);
				if (v == null || Math.round(v * 100) == 0) continue;
				drawer.addView(ShiftUi.row(c, "   " + BA.str(flowLabel(key)), Shifts.signed(v)));
			}
		}
		if (cash.getExpected() != null) {
			drawer.addView(ShiftUi.row(c, BA.str(R.string.shift_expected), Shifts.money(cash.getExpected()), R.color.White, true));
		}
		if (r.isFinal()) {
			if (cash.getCounted() != null) {
				double diff = cash.getDifference() != null ? cash.getDifference() : 0;
				drawer.addView(ShiftUi.row(c, BA.str(R.string.shift_counted), Shifts.money(cash.getCounted()), R.color.White, true));
				drawer.addView(ShiftUi.row(c, BA.str(R.string.shift_difference),
						Shifts.signed(diff) + " · " + Shifts.toneText(diff), Shifts.toneColor(diff), true));
			} else {
				drawer.addView(ShiftUi.row(c, BA.str(R.string.shift_counted), BA.str(R.string.shift_not_counted)));
			}
			if (r.getComment() != null && !r.getComment().isEmpty()) {
				drawer.addView(ShiftUi.label(c, BA.str(R.string.comment)));
				drawer.addView(ShiftUi.value(c, r.getComment()));
			}
			drawer.addView(ShiftUi.row(c, BA.str(R.string.shift_left), Shifts.money(cash.getLeft())));
			drawer.addView(ShiftUi.row(c, BA.str(R.string.shift_removed), Shifts.money(cash.getRemoved())));
		} else if (cash.getExpected() == null) {
			drawer.addView(ShiftUi.hint(c, BA.str(R.string.shift_blind_hint)));
		}
		into.addView(drawer);

		// cash moves
		if (r.getCashMoves() != null && !r.getCashMoves().isEmpty()) {
			LinearLayout moves = ShiftUi.card(c);
			moves.addView(ShiftUi.title(c, BA.str(R.string.shift_cash_moves)));
			for (ShiftReport.CashMove m : r.getCashMoves()) {
				StringBuilder label = new StringBuilder(Shifts.time(m.getTs())).append("  ").append(Shifts.cashKindLabel(m.getKind()));
				if (m.getStaffName() != null) label.append(" · ").append(m.getStaffName());
				if (m.getReason() != null) label.append(" · ").append(m.getReason());
				moves.addView(ShiftUi.row(c, label.toString(), Shifts.signed(m.getSignedAmount())));
			}
			into.addView(moves);
		}

		// expenses
		ShiftReport.Expenses expenses = r.getExpenses();
		if (expenses.getItems() != null && !expenses.getItems().isEmpty()) {
			LinearLayout e = ShiftUi.card(c);
			e.addView(ShiftUi.title(c, BA.str(R.string.shift_expenses) + " · " + Shifts.money(expenses.getTotal())));
			for (ShiftReport.Expense x : expenses.getItems()) {
				String label = Shifts.time(x.getTs()) + "  " + (x.getDescription() != null ? x.getDescription() : "—")
						+ (x.isCash() ? "" : " (" + BA.str(R.string.shift_not_cash) + ")");
				e.addView(ShiftUi.row(c, label, Shifts.money(x.getAmount())));
			}
			into.addView(e);
		}

		// washers
		if (r.getWashers() != null && !r.getWashers().isEmpty()) {
			LinearLayout ws = ShiftUi.card(c);
			ws.addView(ShiftUi.title(c, BA.str(R.string.shift_washer_pay)));
			for (ShiftReport.WasherPay p : r.getWashers()) {
				String name = p.getName() + (p.isOnShift() ? "" : " (" + BA.str(R.string.shift_not_on_shift) + ")");
				ws.addView(ShiftUi.row(c, name, BA.str(R.string.shift_washer_line, p.getCars(),
						Shifts.money(p.getEarned()), Shifts.money(p.getPaid()))));
			}
			into.addView(ws);
		}

		// boxes
		if (r.getBoxes() != null && !r.getBoxes().isEmpty()) {
			LinearLayout bs = ShiftUi.card(c);
			bs.addView(ShiftUi.title(c, BA.str(R.string.shift_boxes)));
			for (ShiftReport.BoxStat b : r.getBoxes()) {
				String time = b.getAvgMinutes() == null ? "—" : BA.str(R.string.shift_minutes, Shifts.plain(b.getAvgMinutes()));
				bs.addView(ShiftUi.row(c, b.getName(), BA.str(R.string.shift_box_line, b.getCars(), time)));
			}
			into.addView(bs);
		}
	}
}
