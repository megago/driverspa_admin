package com.driverspa.admin.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.activity.AdminShiftReportActivity;
import com.driverspa.admin.shift.ShiftApi;
import com.driverspa.admin.shift.ShiftCloseWizard;
import com.driverspa.admin.shift.ShiftReportView;
import com.driverspa.admin.shift.ShiftState;
import com.driverspa.admin.shift.ShiftUi;
import com.driverspa.admin.shift.Shifts;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.model.shift.Shift;
import com.driverspa.model.shift.ShiftCurrent;
import com.driverspa.model.shift.ShiftReport;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * Текущая смена (docs/shift.md): with no shift, the opening (the counted float against what the
 * last shift left, a comment above the limit, the crew); with a shift open, its status, the cash
 * operations (in, out, expense, payout to a washer), the crew, the X-report and the close
 * wizard. Operators never see the expected cash before their count.
 */
public class ShiftCurrentFragment extends AdminBaseFragment {

	private LinearLayout content;
	private Washer washer;
	private User user;
	private boolean userReceived;
	private ShiftCurrent current;
	private ShiftReport report;
	private boolean loading;
	private int seq;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		content = ShiftUi.column(requireContext(), 12);
		return ShiftUi.scroll(requireContext(), content);
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
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		load();
	}

	@Subscribe
	public void onUserReceived(UserGetSelfResponseEvent event) {
		user = event.getUser();
		userReceived = true;
		load();
	}

	private boolean manager() {
		return Shifts.isManager(washer, user);
	}

	private boolean alive() {
		return isAdded() && content != null && ShiftUi.alive(getActivity());
	}

	/** Reads the current shift (and, while one is open, its X-report) and draws the screen. */
	void load() {
		if (washer == null || !userReceived || loading) return;
		if (!Shifts.mayUse(washer, user)) return;
		loading = true;
		final int mySeq = ++seq;
		if (current == null) showMessage(BA.str(R.string.loading_dots));
		final String washerId = washer.getId();
		ShiftApi.current(washerId, (data, error) -> {
			loading = false;
			if (!alive() || mySeq != seq) return;
			if (data == null) {
				showRetry(error);
				return;
			}
			current = data;
			ShiftState.set(washerId, data);
			Shift s = data.getShift();
			if (s != null && !s.isLegacy()) {
				ShiftApi.report(s.getId(), (r, err) -> {
					if (!alive() || mySeq != seq) return;
					report = r;
					render();
				});
			} else {
				report = null;
			}
			render();
		});
	}

	private void showMessage(String text) {
		content.removeAllViews();
		content.addView(ShiftUi.value(requireContext(), text));
	}

	private void showRetry(String error) {
		content.removeAllViews();
		content.addView(ShiftUi.value(requireContext(), error != null ? error : BA.str(R.string.shift_load_error)));
		TextView retry = ShiftUi.button(requireContext(), BA.str(R.string.shift_retry), true);
		retry.setOnClickListener(v -> load());
		content.addView(retry);
	}

	private void render() {
		if (!alive() || current == null) return;
		content.removeAllViews();
		Shift s = current.getShift();
		if (s == null) renderOpenForm();
		else if (s.isLegacy()) renderLegacy(s);
		else renderOpen(s);
	}

	// ---- no shift: open one ----

	private void renderOpenForm() {
		final android.content.Context c = requireContext();
		final int limit = current.getSettings() != null ? current.getSettings().getDiscrepancyLimit() : 0;
		final ShiftCurrent.Previous previous = current.getPrevious();
		LinearLayout card = ShiftUi.card(c);
		card.addView(ShiftUi.title(c, BA.str(R.string.shift_open)));
		if (previous != null && previous.getCashLeft() != null) {
			card.addView(ShiftUi.value(c, BA.str(R.string.shift_handover, previous.getNumberText(), Shifts.money(previous.getCashLeft()))));
		} else {
			card.addView(ShiftUi.hint(c, BA.str(R.string.shift_first)));
		}

		card.addView(ShiftUi.label(c, BA.str(R.string.shift_cash_now)));
		final EditText cash = ShiftUi.amountField(c);
		card.addView(cash);
		card.addView(ShiftUi.hint(c, BA.str(R.string.shift_cash_now_hint)));
		final TextView diff = ShiftUi.hint(c, "");
		diff.setVisibility(View.GONE);
		card.addView(diff);
		final TextView commentLabel = ShiftUi.label(c, BA.str(R.string.comment));
		final EditText comment = ShiftUi.textField(c, BA.str(R.string.shift_comment_hint, Shifts.money(limit)));
		commentLabel.setVisibility(View.GONE);
		comment.setVisibility(View.GONE);
		card.addView(commentLabel);
		card.addView(comment);

		cash.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable e) {
				Double amount = Shifts.parseCash(e.toString());
				Double left = previous != null ? previous.getCashLeft() : null;
				if (amount == null || left == null) {
					diff.setVisibility(View.GONE);
					commentLabel.setVisibility(View.GONE);
					comment.setVisibility(View.GONE);
					return;
				}
				double d = Math.round((amount - left) * 100) / 100.0;
				diff.setText(BA.str(R.string.shift_difference) + ": " + Shifts.signed(d));
				diff.setTextColor(getResources().getColor(Shifts.toneColor(d)));
				diff.setVisibility(View.VISIBLE);
				int show = Shifts.aboveLimit(d, limit) ? View.VISIBLE : View.GONE;
				commentLabel.setVisibility(show);
				comment.setVisibility(show);
			}
		});

		card.addView(ShiftUi.label(c, BA.str(R.string.shift_crew)));
		final List<CheckBox> crewBoxes = new ArrayList<>();
		final List<String> crewUids = new ArrayList<>();
		if (current.getWashers() == null || current.getWashers().isEmpty()) {
			card.addView(ShiftUi.hint(c, BA.str(R.string.shift_no_washers)));
		} else {
			for (ShiftCurrent.Washer w : current.getWashers()) {
				CheckBox box = ShiftUi.check(c, w.getName(), true);
				crewBoxes.add(box);
				crewUids.add(w.getUid());
				card.addView(box);
			}
		}
		final TextView error = ShiftUi.error(c);
		card.addView(error);
		final TextView open = ShiftUi.button(c, BA.str(R.string.shift_open), true);
		card.addView(open);
		content.addView(card);

		open.setOnClickListener(v -> {
			Double amount = Shifts.parseCash(cash.getText().toString());
			if (amount == null) {
				ShiftUi.showError(error, BA.str(R.string.shift_err_amount));
				return;
			}
			String text = comment.getText().toString().trim();
			if (comment.getVisibility() == View.VISIBLE && text.isEmpty()) {
				ShiftUi.showError(error, BA.str(R.string.shift_err_comment));
				return;
			}
			List<String> crew = new ArrayList<>();
			for (int i = 0; i < crewBoxes.size(); i++) if (crewBoxes.get(i).isChecked()) crew.add(crewUids.get(i));
			ShiftUi.showError(error, null);
			open.setEnabled(false);
			ShiftApi.open(washer.getId(), amount, crew, text, (shift, err) -> {
				if (!alive()) return;
				open.setEnabled(true);
				if (shift == null) {
					ShiftUi.showError(error, err);
					return;
				}
				ToastUtil.display(getActivity(), BA.str(R.string.shift_opened_toast));
				load();
			});
		});
	}

	// ---- an open shift from before the cash drawer ----

	private void renderLegacy(final Shift s) {
		android.content.Context c = requireContext();
		LinearLayout card = ShiftUi.card(c);
		card.addView(ShiftUi.title(c, BA.str(R.string.shift_legacy_open)));
		card.addView(ShiftUi.hint(c, BA.str(R.string.shift_legacy_hint)));
		TextView close = ShiftUi.button(c, BA.str(R.string.shift_close), true);
		close.setOnClickListener(v -> ShiftUi.dialog(requireActivity())
				.setTitle(BA.str(R.string.shift_legacy_close_q))
				.setNegativeButton(BA.str(R.string.no_word), null)
				.setPositiveButton(BA.str(R.string.shift_close), (d, w) -> ShiftApi.close(s.getId(), null, null, (data, err) -> {
					if (!alive()) return;
					ToastUtil.display(getActivity(), data != null ? BA.str(R.string.shift_closed_toast) : err);
					load();
				}))
				.show());
		card.addView(close);
		content.addView(card);
	}

	// ---- the open shift ----

	private void renderOpen(final Shift s) {
		final android.content.Context c = requireContext();
		LinearLayout card = ShiftUi.card(c);
		card.addView(ShiftUi.title(c, Shifts.title(s) + " · " + BA.str(R.string.shift_open_chip)));
		card.addView(ShiftUi.hint(c, BA.str(R.string.shift_opened_by, Shifts.dateTime(s.getStartTime()),
				s.getUserName() != null ? s.getUserName() : "") + " · " + Shifts.duration(s.getDuration())));
		if (s.isOverdue()) {
			TextView overdue = ShiftUi.text(c, BA.str(R.string.shift_overdue, Shifts.time(s.getDueTime())), R.color.Orange, R.dimen.text_small);
			card.addView(overdue);
		}
		card.addView(ShiftUi.row(c, BA.str(R.string.shift_opening_cash), Shifts.money(s.getOpeningFloat())));
		if (s.getOpeningDifference() != null && Math.round(s.getOpeningDifference() * 100) != 0) {
			card.addView(ShiftUi.row(c, BA.str(R.string.shift_opening_difference), Shifts.signed(s.getOpeningDifference()),
					Shifts.toneColor(s.getOpeningDifference()), false));
			if (s.getOpeningComment() != null && !s.getOpeningComment().isEmpty()) {
				card.addView(ShiftUi.hint(c, s.getOpeningComment()));
			}
		}
		// The expected cash: managers only (the operator counts blind).
		if (manager() && s.getExpectedCash() != null) {
			card.addView(ShiftUi.row(c, BA.str(R.string.shift_expected), Shifts.money(s.getExpectedCash()), R.color.White, true));
		}
		card.addView(ShiftUi.label(c, BA.str(R.string.shift_crew)));
		StringBuilder crew = new StringBuilder();
		if (s.getCrew() != null) {
			for (Shift.CrewMember m : s.getCrew()) {
				if (crew.length() > 0) crew.append(", ");
				crew.append(m.getName());
			}
		}
		card.addView(ShiftUi.value(c, crew.length() > 0 ? crew.toString() : "—"));

		TextView in = ShiftUi.button(c, BA.str(R.string.shift_cash_in), false);
		TextView out = ShiftUi.button(c, BA.str(R.string.shift_cash_out), false);
		TextView expense = ShiftUi.button(c, BA.str(R.string.shift_cash_expense), false);
		TextView payout = ShiftUi.button(c, BA.str(R.string.shift_cash_payout), false);
		TextView crewButton = ShiftUi.button(c, BA.str(R.string.shift_crew_edit), false);
		TextView x = ShiftUi.button(c, BA.str(R.string.shift_x_report), false);
		TextView close = ShiftUi.button(c, BA.str(R.string.shift_close), true);
		in.setOnClickListener(v -> cashDialog(s, "in"));
		out.setOnClickListener(v -> cashDialog(s, "out"));
		expense.setOnClickListener(v -> cashDialog(s, "expense"));
		payout.setOnClickListener(v -> cashDialog(s, "payout"));
		crewButton.setOnClickListener(v -> crewDialog(s));
		x.setOnClickListener(v -> xReport(s));
		close.setOnClickListener(v -> ShiftCloseWizard.start(requireActivity(), s, () -> {
			if (alive()) load();
		}));
		card.addView(ShiftUi.buttons(c, in, out));
		card.addView(ShiftUi.buttons(c, expense, payout));
		card.addView(ShiftUi.buttons(c, crewButton, x));
		card.addView(close);
		content.addView(card);

		if (report != null && report.getCashMoves() != null && !report.getCashMoves().isEmpty()) {
			LinearLayout moves = ShiftUi.card(c);
			moves.addView(ShiftUi.title(c, BA.str(R.string.shift_cash_moves)));
			for (ShiftReport.CashMove m : report.getCashMoves()) {
				StringBuilder label = new StringBuilder(Shifts.time(m.getTs())).append("  ").append(Shifts.cashKindLabel(m.getKind()));
				if (m.getStaffName() != null) label.append(" · ").append(m.getStaffName());
				if (m.getReason() != null) label.append(" · ").append(m.getReason());
				moves.addView(ShiftUi.row(c, label.toString(), Shifts.signed(m.getSignedAmount())));
			}
			content.addView(moves);
		}
	}

	/** Внесение, Изъятие, Расход or Выплата мойщику: amount and a reason (a washer for a payout). */
	private void cashDialog(final Shift s, final String kind) {
		final Activity activity = requireActivity();
		final android.content.Context c = activity;
		LinearLayout box = ShiftUi.column(c, 20);
		box.addView(ShiftUi.hint(c, BA.str(hintOf(kind))));
		final List<ShiftCurrent.Washer> washers = current.getWashers() != null ? current.getWashers() : new ArrayList<>();
		final android.widget.Spinner staff = ShiftUi.spinner(c);
		if ("payout".equals(kind)) {
			box.addView(ShiftUi.label(c, BA.str(R.string.shift_washer)));
			List<String> names = new ArrayList<>();
			for (ShiftCurrent.Washer w : washers) names.add(w.getName());
			staff.setAdapter(ShiftUi.spinnerAdapter(c, names));
			box.addView(staff);
		}
		box.addView(ShiftUi.label(c, BA.str(R.string.amount)));
		final EditText amount = ShiftUi.amountField(c);
		box.addView(amount);
		box.addView(ShiftUi.label(c, BA.str(R.string.shift_reason)));
		final EditText reason = ShiftUi.textField(c, BA.str(R.string.shift_reason));
		box.addView(reason);
		final TextView error = ShiftUi.error(c);
		box.addView(error);
		final String clientUid = Shifts.newClientUid();

		final AlertDialog dialog = ShiftUi.dialog(activity)
				.setTitle(Shifts.cashKindLabel(kind))
				.setView(ShiftUi.scroll(c, box))
				.setNegativeButton(BA.str(R.string.cancel_word), null)
				.setPositiveButton(BA.str(R.string.save), null)
				.create();
		dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
			Double value = Shifts.parseCash(amount.getText().toString());
			String text = reason.getText().toString().trim();
			if (value == null || value <= 0) {
				ShiftUi.showError(error, BA.str(R.string.shift_err_amount));
				return;
			}
			if (text.isEmpty()) {
				ShiftUi.showError(error, BA.str(R.string.shift_err_reason));
				return;
			}
			String staffUid = null;
			if ("payout".equals(kind)) {
				int i = staff.getSelectedItemPosition();
				if (i < 0 || i >= washers.size()) {
					ShiftUi.showError(error, BA.str(R.string.shift_err_pick_washer));
					return;
				}
				staffUid = washers.get(i).getUid();
			}
			v.setEnabled(false);
			ShiftApi.cash(s.getId(), kind, value, text, staffUid, clientUid, (data, err) -> {
				v.setEnabled(true);
				if (data == null) {
					ShiftUi.showError(error, err);
					return;
				}
				dialog.dismiss();
				if (alive()) {
					ToastUtil.display(getActivity(), BA.str(R.string.shift_cash_saved, Shifts.money(value)));
					load();
				}
			});
		}));
		dialog.show();
	}

	private static int hintOf(String kind) {
		switch (kind) {
			case "out": return R.string.shift_cash_hint_out;
			case "expense": return R.string.shift_cash_hint_expense;
			case "payout": return R.string.shift_cash_hint_payout;
			default: return R.string.shift_cash_hint_in;
		}
	}

	/** The crew: only these washers are offered when assigning a box. */
	private void crewDialog(final Shift s) {
		final Activity activity = requireActivity();
		LinearLayout box = ShiftUi.column(activity, 20);
		box.addView(ShiftUi.hint(activity, BA.str(R.string.shift_crew_hint)));
		final List<CheckBox> boxes = new ArrayList<>();
		final List<String> uids = new ArrayList<>();
		List<String> crew = new ArrayList<>();
		if (s.getCrew() != null) for (Shift.CrewMember m : s.getCrew()) crew.add(m.getUid());
		if (current.getWashers() != null) {
			for (ShiftCurrent.Washer w : current.getWashers()) {
				CheckBox check = ShiftUi.check(activity, w.getName(), crew.contains(w.getUid()));
				boxes.add(check);
				uids.add(w.getUid());
				box.addView(check);
			}
		}
		if (boxes.isEmpty()) box.addView(ShiftUi.hint(activity, BA.str(R.string.shift_no_washers)));
		final TextView error = ShiftUi.error(activity);
		box.addView(error);
		final AlertDialog dialog = ShiftUi.dialog(activity)
				.setTitle(BA.str(R.string.shift_crew))
				.setView(ShiftUi.scroll(activity, box))
				.setNegativeButton(BA.str(R.string.cancel_word), null)
				.setPositiveButton(BA.str(R.string.save), null)
				.create();
		dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
			List<String> picked = new ArrayList<>();
			for (int i = 0; i < boxes.size(); i++) if (boxes.get(i).isChecked()) picked.add(uids.get(i));
			v.setEnabled(false);
			ShiftApi.crew(s.getId(), picked, (data, err) -> {
				v.setEnabled(true);
				if (data == null) {
					ShiftUi.showError(error, err);
					return;
				}
				dialog.dismiss();
				if (alive()) {
					ToastUtil.display(getActivity(), BA.str(R.string.shift_crew_saved));
					load();
				}
			});
		}));
		dialog.show();
	}

	/** The X-report, fresh (without the cash figures for an operator: the backend leaves them out). */
	private void xReport(final Shift s) {
		final Activity activity = requireActivity();
		final android.app.ProgressDialog progress = ShiftUi.progress(activity);
		progress.show();
		ShiftApi.report(s.getId(), (r, err) -> {
			if (progress.isShowing()) progress.dismiss();
			if (!ShiftUi.alive(activity)) return;
			if (r == null) {
				ToastUtil.display(activity, err);
				return;
			}
			LinearLayout box = ShiftUi.column(activity, 16);
			ShiftReportView.render(activity, box, r);
			ShiftUi.dialog(activity)
					.setTitle(BA.str(R.string.shift_x_report) + " · " + Shifts.title(s))
					.setView(ShiftUi.scroll(activity, box))
					.setPositiveButton(BA.str(R.string.close), null)
					.show();
		});
	}

	/** Opens a shift's report screen. */
	public static void openReport(Activity activity, String shiftId) {
		activity.startActivity(new Intent(activity, AdminShiftReportActivity.class).putExtra(AdminShiftReportActivity.EXTRA_SHIFT_ID, shiftId));
	}
}
