package com.driverspa.model.shift;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;
import java.util.List;

/** work_shift/current: the open shift (any kind), the last closed one, settings and washers. */
public class ShiftCurrent {

	Shift shift;
	Previous previous;
	ShiftSettings settings;
	List<Washer> washers;

	public Shift getShift() {
		return shift;
	}

	public Previous getPrevious() {
		return previous;
	}

	public ShiftSettings getSettings() {
		return settings;
	}

	public List<Washer> getWashers() {
		return washers;
	}

	/** The last closed cash-drawer shift: what it left in the drawer is the expected float. */
	public static class Previous {
		String id;
		Integer number;
		@SerializedName("end_time")
		Instant endTime;
		@SerializedName("cash_left")
		Double cashLeft;
		@SerializedName("closed_user_name")
		String closedUserName;

		public String getId() {
			return id;
		}

		public String getNumberText() {
			return number != null ? String.valueOf(number) : "";
		}

		public Instant getEndTime() {
			return endTime;
		}

		public Double getCashLeft() {
			return cashLeft;
		}

		public String getClosedUserName() {
			return closedUserName;
		}
	}

	/** A washer on duty (role carwasher, working or on leave): the crew is picked from these. */
	public static class Washer {
		String uid;
		String name;
		String status;

		public String getUid() {
			return uid;
		}

		public String getName() {
			return name;
		}
	}
}
