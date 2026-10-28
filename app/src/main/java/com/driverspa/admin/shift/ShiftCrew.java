package com.driverspa.admin.shift;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.adapter.StaffGridAdapter;
import com.driverspa.model.Staff;

import java.util.ArrayList;
import java.util.List;

/**
 * The washer picker of the order forms (docs/shift.md, crew): while a shift is open with a crew,
 * only the crew is offered, plus anyone already on the order, under a hint with "Показать всех".
 * With no shift, or an empty crew, everyone, as before.
 */
public final class ShiftCrew {

	private static final String HINT_TAG = "shift_crew_hint";

	private ShiftCrew() {
	}

	/**
	 * The staff to offer in `grid`. `rebuild` draws the grid again (after "Показать всех").
	 */
	public static ArrayList<Staff> filter(final View grid, String washerId, ArrayList<Staff> all, List<String> keep,
										  final Runnable rebuild) {
		TextView hint = hint(grid);
		List<String> crew = ShiftState.crew(washerId);
		boolean showAll = Boolean.TRUE.equals(grid.getTag(R.id.shift_crew_show_all));
		if (crew == null || crew.isEmpty() || showAll) {
			if (hint != null) hint.setVisibility(View.GONE);
			return all;
		}
		ArrayList<Staff> offered = new ArrayList<>();
		for (Staff s : all) {
			if (crew.contains(s.getUid()) || (keep != null && keep.contains(s.getUid()))) offered.add(s);
		}
		if (hint != null) {
			hint.setText(BA.str(R.string.shift_crew_only) + "  " + BA.str(R.string.shift_show_all_staff));
			hint.setVisibility(View.VISIBLE);
			hint.setOnClickListener(v -> {
				grid.setTag(R.id.shift_crew_show_all, Boolean.TRUE);
				rebuild.run();
			});
		}
		return offered;
	}

	/** Marks `uids` selected again (a grid drawn anew keeps what was picked). */
	public static void reselect(StaffGridAdapter adapter, List<String> uids) {
		if (uids == null || uids.isEmpty()) return;
		for (int i = 0; i < adapter.getData().size(); i++) {
			if (uids.contains(adapter.getData().get(i).getUid())) adapter.setSelectedPosition(i, true);
		}
		adapter.notifyDataSetChanged();
	}

	/** The hint under the grid, made once and kept as the grid's next sibling. */
	private static TextView hint(View grid) {
		if (!(grid.getParent() instanceof ViewGroup)) return null;
		ViewGroup parent = (ViewGroup) grid.getParent();
		View existing = parent.findViewWithTag(HINT_TAG);
		if (existing instanceof TextView) return (TextView) existing;
		TextView hint = ShiftUi.text(grid.getContext(), "", R.color.BlueLight, R.dimen.text_small);
		hint.setTag(HINT_TAG);
		int pad = ShiftUi.dp(grid.getContext(), 8);
		hint.setPadding(pad, pad, pad, pad);
		hint.setMinHeight(ShiftUi.dp(grid.getContext(), 44));
		hint.setVisibility(View.GONE);
		parent.addView(hint, parent.indexOfChild(grid) + 1);
		return hint;
	}
}
