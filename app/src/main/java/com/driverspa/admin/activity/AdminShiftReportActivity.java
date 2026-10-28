package com.driverspa.admin.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.shift.ShiftApi;
import com.driverspa.admin.shift.ShiftPdf;
import com.driverspa.admin.shift.ShiftReportView;
import com.driverspa.admin.shift.ShiftUi;
import com.driverspa.admin.shift.Shifts;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.model.shift.Shift;
import com.driverspa.model.shift.ShiftReport;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.squareup.otto.Subscribe;

/**
 * One shift (from the close or from История): who opened and closed it, the opening, every
 * count, the Z-report (an X-report while open), "PDF" (PdfDocument, shared) and, for the owner
 * and directors, corrections: a note, or a write-off of the difference, kept as an audit trail.
 */
public class AdminShiftReportActivity extends AdminBaseActivity {

	public static final String EXTRA_SHIFT_ID = "EXTRA_SHIFT_ID";

	private LinearLayout content;
	private String shiftId;
	private ShiftReport report;
	private Washer washer;
	private User user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_admin_shift_report);
		Toolbar toolbar = findViewById(R.id.toolbar_actionbar);
		TextView titleView = toolbar.findViewById(R.id.action_bar_title);
		titleView.setText(BA.str(R.string.shift_menu));
		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowTitleEnabled(false);
		}
		shiftId = getIntent().getStringExtra(EXTRA_SHIFT_ID);
		content = ShiftUi.column(this, 12);
		FrameLayout holder = findViewById(R.id.shiftContent);
		holder.addView(ShiftUi.scroll(this, content));
		installFinishOnBackCallback();
		load();
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
		render();
	}

	@Subscribe
	public void onUserReceived(UserGetSelfResponseEvent event) {
		user = event.getUser();
		render();
	}

	private void load() {
		if (shiftId == null) {
			finish();
			return;
		}
		content.removeAllViews();
		content.addView(ShiftUi.value(this, BA.str(R.string.loading_dots)));
		ShiftApi.report(shiftId, (r, error) -> {
			if (!ShiftUi.alive(this)) return;
			if (r == null) {
				content.removeAllViews();
				content.addView(ShiftUi.value(this, error));
				TextView retry = ShiftUi.button(this, BA.str(R.string.shift_retry), true);
				retry.setOnClickListener(v -> load());
				content.addView(retry);
				return;
			}
			report = r;
			render();
		});
	}

	private void render() {
		if (report == null || !ShiftUi.alive(this)) return;
		content.removeAllViews();
		Shift s = report.getInfo();
		content.addView(ShiftUi.title(this, BA.str(report.isFinal() ? R.string.shift_z_report : R.string.shift_x_report)
				+ " · " + Shifts.title(s)));
		if (report.isFinal()) {
			TextView pdf = ShiftUi.button(this, BA.str(R.string.shift_pdf), true);
			pdf.setOnClickListener(v -> ShiftPdf.share(this, report));
			content.addView(pdf);
		}
		if (s != null) renderInfo(s);
		ShiftReportView.render(this, content, report);
		if (s != null && !s.isOpen() && !s.isLegacy()) renderCorrections(s);
	}

	private void renderInfo(Shift s) {
		LinearLayout card = ShiftUi.card(this);
		card.addView(ShiftUi.row(this, BA.str(R.string.shift_opened), Shifts.dateTime(s.getStartTime()) + " · " + nz(s.getUserName())));
		if (s.getEndTime() != null) {
			String closed = Shifts.dateTime(s.getEndTime()) + " · " + nz(s.getClosedUserName());
			if (s.getCloseKind() != null && !Shift.KIND_OPERATOR.equals(s.getCloseKind())) closed += " (" + Shifts.closeKindLabel(s.getCloseKind()) + ")";
			card.addView(ShiftUi.row(this, BA.str(R.string.shift_closed), closed));
		}
		card.addView(ShiftUi.row(this, BA.str(R.string.shift_duration), Shifts.duration(s.getDuration())));
		if (s.getOpeningExpected() != null) card.addView(ShiftUi.row(this, BA.str(R.string.shift_opening_expected), Shifts.money(s.getOpeningExpected())));
		card.addView(ShiftUi.row(this, BA.str(R.string.shift_opening_counted),
				s.getOpeningCash() != null ? Shifts.money(s.getOpeningCash()) : BA.str(R.string.shift_not_counted)));
		if (s.getOpeningDifference() != null && Math.round(s.getOpeningDifference() * 100) != 0) {
			card.addView(ShiftUi.row(this, BA.str(R.string.shift_opening_difference), Shifts.signed(s.getOpeningDifference()),
					Shifts.toneColor(s.getOpeningDifference()), true));
			if (s.getOpeningComment() != null) card.addView(ShiftUi.hint(this, s.getOpeningComment()));
		}
		if (s.getCrew() != null && !s.getCrew().isEmpty()) {
			StringBuilder crew = new StringBuilder();
			for (Shift.CrewMember m : s.getCrew()) {
				if (crew.length() > 0) crew.append(", ");
				crew.append(m.getName());
			}
			card.addView(ShiftUi.label(this, BA.str(R.string.shift_crew)));
			card.addView(ShiftUi.value(this, crew.toString()));
		}
		if (s.getCounts() != null && !s.getCounts().isEmpty()) {
			card.addView(ShiftUi.label(this, BA.str(R.string.shift_counts)));
			for (Shift.Count c : s.getCounts()) {
				card.addView(ShiftUi.hint(this, Shifts.dateTime(c.getTs()) + " · " + nz(c.getUserName())));
				card.addView(ShiftUi.row(this, BA.str(R.string.shift_count_line, Shifts.money(c.getCounted()), Shifts.money(c.getExpected())),
						Shifts.signed(c.getDifference()), Shifts.toneColor(c.getDifference()), true));
			}
		}
		content.addView(card);
	}

	private void renderCorrections(final Shift s) {
		LinearLayout card = ShiftUi.card(this);
		card.addView(ShiftUi.title(this, BA.str(R.string.shift_corrections)));
		double done = 0;
		if (s.getCorrections() == null || s.getCorrections().isEmpty()) {
			card.addView(ShiftUi.hint(this, BA.str(R.string.shift_no_corrections)));
		} else {
			for (Shift.Correction c : s.getCorrections()) {
				if (c.isWriteoff()) done += c.getAmount();
				card.addView(ShiftUi.hint(this, Shifts.dateTime(c.getTs()) + " · " + nz(c.getUserName())));
				String what = c.isWriteoff() ? BA.str(R.string.shift_corr_writeoff) + " · " + Shifts.money(c.getAmount())
						: BA.str(R.string.shift_corr_note);
				card.addView(ShiftUi.value(this, what + " · " + nz(c.getComment())));
			}
		}
		if (Shifts.isManager(washer, user)) {
			final double left = s.getDifference() != null
					? Math.max(0, Math.round((Math.abs(s.getDifference()) - done) * 100) / 100.0) : 0;
			TextView note = ShiftUi.button(this, BA.str(R.string.shift_corr_note), false);
			note.setOnClickListener(v -> correctionDialog(s, Shift.Correction.NOTE, 0));
			if (left > 0) {
				TextView writeoff = ShiftUi.button(this, BA.str(R.string.shift_corr_writeoff), false);
				writeoff.setOnClickListener(v -> correctionDialog(s, Shift.Correction.WRITEOFF, left));
				card.addView(ShiftUi.buttons(this, note, writeoff));
			} else {
				card.addView(note);
			}
		}
		content.addView(card);
	}

	private void correctionDialog(final Shift s, final String kind, final double left) {
		final boolean writeoff = Shift.Correction.WRITEOFF.equals(kind);
		LinearLayout box = ShiftUi.column(this, 20);
		final EditText amount = ShiftUi.amountField(this);
		if (writeoff) {
			boolean shortage = s.getDifference() != null && s.getDifference() < 0;
			box.addView(ShiftUi.hint(this, BA.str(shortage ? R.string.shift_writeoff_short_hint : R.string.shift_writeoff_over_hint, Shifts.money(left))));
			box.addView(ShiftUi.label(this, BA.str(R.string.amount)));
			amount.setText(Shifts.plain(left));
			box.addView(amount);
		}
		box.addView(ShiftUi.label(this, BA.str(R.string.comment)));
		final EditText comment = ShiftUi.textField(this, "");
		box.addView(comment);
		final TextView error = ShiftUi.error(this);
		box.addView(error);
		final AlertDialog dialog = ShiftUi.dialog(this)
				.setTitle(BA.str(writeoff ? R.string.shift_corr_writeoff : R.string.shift_corr_note))
				.setView(ShiftUi.scroll(this, box))
				.setNegativeButton(BA.str(R.string.cancel_word), null)
				.setPositiveButton(BA.str(R.string.shift_corr_add), null)
				.create();
		dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
			String text = comment.getText().toString().trim();
			if (text.isEmpty()) {
				ShiftUi.showError(error, BA.str(R.string.shift_err_comment_empty));
				return;
			}
			Double value = null;
			if (writeoff) {
				value = Shifts.parseCash(amount.getText().toString());
				if (value == null || value <= 0 || value > left) {
					ShiftUi.showError(error, BA.str(R.string.shift_err_amount));
					return;
				}
			}
			v.setEnabled(false);
			ShiftApi.correction(s.getId(), kind, text, value, (data, err) -> {
				v.setEnabled(true);
				if (data == null) {
					ShiftUi.showError(error, err);
					return;
				}
				dialog.dismiss();
				if (!ShiftUi.alive(this)) return;
				ToastUtil.display(this, BA.str(R.string.shift_corr_saved));
				load();
			});
		}));
		dialog.show();
	}

	private static String nz(String s) {
		return s != null ? s : "—";
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			overridePendingTransitionWithCommonCloseTransition();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
