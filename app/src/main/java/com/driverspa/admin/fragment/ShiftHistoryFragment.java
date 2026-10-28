package com.driverspa.admin.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.shift.ShiftApi;
import com.driverspa.admin.shift.ShiftUi;
import com.driverspa.admin.shift.Shifts;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.model.shift.Shift;
import com.driverspa.model.shift.ShiftResponses;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.squareup.otto.Subscribe;

/**
 * Смены: newest first, 20 a page, with who opened and closed each, the duration, revenue and
 * difference; "Только недостачи". A row opens the shift (its Z-report, counts, corrections).
 */
public class ShiftHistoryFragment extends AdminBaseFragment {

	private static final int PAGE = 20;
	private static final String STATE_PAGE = "STATE_PAGE";
	private static final String STATE_SHORTAGE = "STATE_SHORTAGE";

	private LinearLayout content;
	private LinearLayout list;
	private TextView pager;
	private Washer washer;
	private User user;
	private boolean userReceived;
	private int page;
	private boolean shortage;
	private int total;
	private int seq;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			page = savedInstanceState.getInt(STATE_PAGE, 0);
			shortage = savedInstanceState.getBoolean(STATE_SHORTAGE, false);
		}
		content = ShiftUi.column(requireContext(), 12);
		CheckBox filter = ShiftUi.check(requireContext(), BA.str(R.string.shift_only_shortages), shortage);
		filter.setOnCheckedChangeListener((b, checked) -> {
			shortage = checked;
			page = 0;
			load();
		});
		content.addView(filter);
		list = new LinearLayout(requireContext());
		list.setOrientation(LinearLayout.VERTICAL);
		content.addView(list);
		pager = ShiftUi.hint(requireContext(), "");
		pager.setGravity(android.view.Gravity.CENTER);
		TextView prev = ShiftUi.button(requireContext(), BA.str(R.string.back), false);
		TextView next = ShiftUi.button(requireContext(), BA.str(R.string.next), false);
		prev.setOnClickListener(v -> {
			if (page > 0) {
				page--;
				load();
			}
		});
		next.setOnClickListener(v -> {
			if ((page + 1) * PAGE < total) {
				page++;
				load();
			}
		});
		content.addView(pager);
		content.addView(ShiftUi.buttons(requireContext(), prev, next));
		return ShiftUi.scroll(requireContext(), content);
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_PAGE, page);
		outState.putBoolean(STATE_SHORTAGE, shortage);
	}

	@Override
	public void onResume() {
		super.onResume();
		BA.getEventBus().register(this);
		load();
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

	private void load() {
		if (washer == null || !userReceived || list == null || !Shifts.mayUse(washer, user)) return;
		final int mySeq = ++seq;
		ShiftApi.history(washer.getId(), page * PAGE, PAGE, shortage, (data, error) -> {
			if (!isAdded() || mySeq != seq || list == null) return;
			show(data, error);
		});
	}

	private void show(ShiftResponses.HistoryData data, String error) {
		list.removeAllViews();
		if (data == null) {
			list.addView(ShiftUi.value(requireContext(), error));
			return;
		}
		total = data.getTotalCount();
		int pages = Math.max(1, (total + PAGE - 1) / PAGE);
		pager.setText(BA.str(R.string.shift_total_count, total) + " · " + (page + 1) + " / " + pages);
		if (data.getObjects() == null || data.getObjects().isEmpty()) {
			list.addView(ShiftUi.value(requireContext(), BA.str(shortage ? R.string.shift_no_shortages : R.string.shift_no_history)));
			return;
		}
		for (final Shift s : data.getObjects()) list.addView(row(s));
	}

	private View row(final Shift s) {
		android.content.Context c = requireContext();
		LinearLayout card = ShiftUi.card(c);
		String title = (s.getNumber() != null ? "№" + s.getNumberText() : BA.str(R.string.shift_old)) + " · " + Shifts.dateTime(s.getStartTime());
		if (s.isOpen()) title += " · " + BA.str(R.string.shift_open_chip);
		else if (s.getCloseKind() != null && !Shift.KIND_OPERATOR.equals(s.getCloseKind())) {
			title += " · " + Shifts.closeKindLabel(s.getCloseKind());
		}
		card.addView(ShiftUi.title(c, title));
		String who = (s.getUserName() != null ? s.getUserName() : "—");
		if (!s.isOpen()) who += " → " + (s.getClosedUserName() != null ? s.getClosedUserName() : Shifts.closeKindLabel(Shift.KIND_AUTO));
		card.addView(ShiftUi.hint(c, who + " · " + Shifts.duration(s.getDuration())));
		if (s.getRevenue() != null) card.addView(ShiftUi.row(c, BA.str(R.string.shift_revenue), Shifts.money(s.getRevenue())));
		if (s.getDifference() != null) {
			card.addView(ShiftUi.row(c, BA.str(R.string.shift_difference), Shifts.signed(s.getDifference()),
					Shifts.toneColor(s.getDifference()), true));
		}
		card.setClickable(true);
		card.setOnClickListener(v -> ShiftCurrentFragment.openReport(requireActivity(), s.getId()));
		return card;
	}
}
