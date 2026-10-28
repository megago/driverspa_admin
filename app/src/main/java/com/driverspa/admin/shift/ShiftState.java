package com.driverspa.admin.shift;

import com.driverspa.model.shift.Shift;
import com.driverspa.model.shift.ShiftCurrent;

import java.util.ArrayList;
import java.util.List;

/**
 * The car wash's current shift (work_shift/current), kept for the home screen's notice and the
 * washer pickers (the crew). Read on the home screen's resume and after every shift action.
 * Only for users the backend serves (Shifts.mayUse); a backend without shifts answers with an
 * error and then nothing changes for the user.
 */
public final class ShiftState {

	public interface Listener {
		void onShiftChanged();
	}

	private static ShiftCurrent current;
	private static String carwashId;
	private static final List<Listener> LISTENERS = new ArrayList<>();

	private ShiftState() {
	}

	public static void addListener(Listener listener) {
		if (!LISTENERS.contains(listener)) LISTENERS.add(listener);
	}

	public static void removeListener(Listener listener) {
		LISTENERS.remove(listener);
	}

	/** Reads the current shift of the car wash again. */
	public static void refresh(final String washerId) {
		if (washerId == null) return;
		if (!washerId.equals(carwashId)) {
			current = null;
			carwashId = washerId;
		}
		ShiftApi.current(washerId, (data, error) -> {
			if (!washerId.equals(carwashId)) return;
			if (data != null) set(washerId, data);
		});
	}

	/** A fresh value read by a shift screen. */
	public static void set(String washerId, ShiftCurrent value) {
		carwashId = washerId;
		current = value;
		for (Listener l : new ArrayList<>(LISTENERS)) l.onShiftChanged();
	}

	public static ShiftCurrent get(String washerId) {
		return washerId != null && washerId.equals(carwashId) ? current : null;
	}

	/** The open cash-drawer shift, or null. */
	public static Shift openShift(String washerId) {
		ShiftCurrent c = get(washerId);
		Shift s = c != null ? c.getShift() : null;
		return s != null && !s.isLegacy() ? s : null;
	}

	/** Crew uids of the open shift; null when no shift is open (pick from everyone). */
	public static List<String> crew(String washerId) {
		Shift s = openShift(washerId);
		if (s == null || s.getCrew() == null) return null;
		List<String> uids = new ArrayList<>();
		for (Shift.CrewMember m : s.getCrew()) uids.add(m.getUid());
		return uids;
	}

	/** "Require an open shift" is on and none is open. */
	public static boolean blocked(String washerId) {
		ShiftCurrent c = get(washerId);
		return c != null && c.getSettings() != null && c.getSettings().isRequired() && openShift(washerId) == null;
	}

	/** The open shift when it is past its closing time, else null. */
	public static Shift overdue(String washerId) {
		Shift s = openShift(washerId);
		return s != null && s.isOverdue() ? s : null;
	}
}
