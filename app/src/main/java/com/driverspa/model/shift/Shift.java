package com.driverspa.model.shift;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;
import java.util.List;

/**
 * A cash-drawer shift (docs/shift.md, "Shift"), as work_shift/open, current, history, crew,
 * correction and close return it. The expected cash of an open shift is null unless the user is
 * a manager (the operator counts blind).
 */
public class Shift {

	public static final String OPEN = "open";
	public static final String CLOSED = "closed";

	public static final String KIND_OPERATOR = "operator";
	public static final String KIND_LEGACY = "legacy";
	public static final String KIND_AUTO = "auto";

	String id;
	String carwash;
	Integer number;
	String status;
	/** Opened before the cash drawer: no count, no cash actions; it can only be closed. */
	Boolean legacy;
	@SerializedName("start_time")
	Instant startTime;
	@SerializedName("end_time")
	Instant endTime;
	/** Minutes (live while open). */
	Integer duration;
	String user;
	@SerializedName("user_name")
	String userName;
	@SerializedName("closed_user")
	String closedUser;
	@SerializedName("closed_user_name")
	String closedUserName;
	@SerializedName("close_kind")
	String closeKind;
	@SerializedName("previous_shift")
	String previousShift;
	@SerializedName("opening_expected")
	Double openingExpected;
	@SerializedName("opening_cash")
	Double openingCash;
	@SerializedName("opening_difference")
	Double openingDifference;
	@SerializedName("opening_comment")
	String openingComment;
	List<CrewMember> crew;
	/** Left out of history rows. */
	List<Count> counts;
	@SerializedName("expected_cash")
	Double expectedCash;
	@SerializedName("counted_cash")
	Double countedCash;
	Double difference;
	@SerializedName("close_comment")
	String closeComment;
	@SerializedName("cash_left")
	Double cashLeft;
	@SerializedName("cash_removed")
	Double cashRemoved;
	Double revenue;
	@SerializedName("due_time")
	Instant dueTime;
	Boolean overdue;
	List<Correction> corrections;

	public String getId() {
		return id;
	}

	public String getCarwash() {
		return carwash;
	}

	public Integer getNumber() {
		return number;
	}

	/** "12", or "" when the backend has no number. */
	public String getNumberText() {
		return number != null ? String.valueOf(number) : "";
	}

	public boolean isOpen() {
		return OPEN.equals(status);
	}

	public boolean isLegacy() {
		return Boolean.TRUE.equals(legacy);
	}

	public Instant getStartTime() {
		return startTime;
	}

	public Instant getEndTime() {
		return endTime;
	}

	public int getDuration() {
		return duration != null ? duration : 0;
	}

	public String getUserName() {
		return userName;
	}

	public String getClosedUserName() {
		return closedUserName;
	}

	public String getCloseKind() {
		return closeKind;
	}

	public Double getOpeningExpected() {
		return openingExpected;
	}

	public Double getOpeningCash() {
		return openingCash;
	}

	public Double getOpeningDifference() {
		return openingDifference;
	}

	public String getOpeningComment() {
		return openingComment;
	}

	public List<CrewMember> getCrew() {
		return crew;
	}

	public List<Count> getCounts() {
		return counts;
	}

	public Double getExpectedCash() {
		return expectedCash;
	}

	public Double getCountedCash() {
		return countedCash;
	}

	public Double getDifference() {
		return difference;
	}

	public String getCloseComment() {
		return closeComment;
	}

	public Double getCashLeft() {
		return cashLeft;
	}

	public Double getCashRemoved() {
		return cashRemoved;
	}

	public Double getRevenue() {
		return revenue;
	}

	public Instant getDueTime() {
		return dueTime;
	}

	public boolean isOverdue() {
		return Boolean.TRUE.equals(overdue);
	}

	public List<Correction> getCorrections() {
		return corrections;
	}

	/** The float the drawer started with: the count, else what the previous shift left. */
	public double getOpeningFloat() {
		if (openingCash != null) return openingCash;
		return openingExpected != null ? openingExpected : 0;
	}

	public static class CrewMember {
		String uid;
		String name;

		public String getUid() {
			return uid;
		}

		public String getName() {
			return name;
		}
	}

	/** One blind count at the close (each recount is kept). */
	public static class Count {
		Double counted;
		Double expected;
		Double difference;
		Instant ts;
		@SerializedName("user_name")
		String userName;

		public double getCounted() {
			return counted != null ? counted : 0;
		}

		public double getExpected() {
			return expected != null ? expected : 0;
		}

		public double getDifference() {
			return difference != null ? difference : 0;
		}

		public Instant getTs() {
			return ts;
		}

		public String getUserName() {
			return userName;
		}
	}

	/** An owner's correction of a closed shift: a note, or a write-off of its difference. */
	public static class Correction {
		public static final String NOTE = "note";
		public static final String WRITEOFF = "writeoff";

		String uid;
		String kind;
		Double amount;
		String comment;
		Instant ts;
		@SerializedName("user_name")
		String userName;
		String transaction;

		public String getKind() {
			return kind;
		}

		public boolean isWriteoff() {
			return WRITEOFF.equals(kind);
		}

		public double getAmount() {
			return amount != null ? amount : 0;
		}

		public String getComment() {
			return comment;
		}

		public Instant getTs() {
			return ts;
		}

		public String getUserName() {
			return userName;
		}
	}
}
