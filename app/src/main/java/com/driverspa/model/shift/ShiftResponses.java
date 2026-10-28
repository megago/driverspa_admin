package com.driverspa.model.shift;

import com.driverspa.model.api.response.BaseResponseHolder;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/** The envelopes ({status, data, message}) of the work_shift/... calls (docs/shift.md). */
public final class ShiftResponses {

	private ShiftResponses() {
	}

	public static class Current extends BaseResponseHolder {
		@SerializedName("data")
		ShiftCurrent data;

		public ShiftCurrent getData() {
			return data;
		}
	}

	/** open, crew, correction: the shift. */
	public static class One extends BaseResponseHolder {
		@SerializedName("data")
		Shift data;

		public Shift getData() {
			return data;
		}
	}

	/** close: the shift and its Z-report. */
	public static class Closed extends BaseResponseHolder {
		@SerializedName("data")
		ClosedData data;

		public ClosedData getData() {
			return data;
		}
	}

	public static class ClosedData extends Shift {
		ShiftReport report;

		public ShiftReport getReport() {
			return report;
		}
	}

	public static class Report extends BaseResponseHolder {
		@SerializedName("data")
		ShiftReport data;

		public ShiftReport getData() {
			return data;
		}
	}

	public static class Count extends BaseResponseHolder {
		@SerializedName("data")
		CountResult data;

		public CountResult getData() {
			return data;
		}
	}

	/** The answer of a blind count: only now the expected cash is revealed. */
	public static class CountResult {
		@SerializedName("counted_cash")
		Double countedCash;
		@SerializedName("expected_cash")
		Double expectedCash;
		Double difference;
		Integer limit;
		@SerializedName("comment_required")
		Boolean commentRequired;
		@SerializedName("open_orders")
		ShiftReport.OpenOrders openOrders;

		public double getCountedCash() {
			return countedCash != null ? countedCash : 0;
		}

		public double getExpectedCash() {
			return expectedCash != null ? expectedCash : 0;
		}

		public double getDifference() {
			return difference != null ? difference : 0;
		}

		public int getLimit() {
			return limit != null ? limit : 0;
		}

		public boolean isCommentRequired() {
			return Boolean.TRUE.equals(commentRequired);
		}
	}

	public static class Cash extends BaseResponseHolder {
		@SerializedName("data")
		CashResult data;

		public CashResult getData() {
			return data;
		}
	}

	public static class CashResult {
		/** False for a repeated client_uid: not applied again. */
		Boolean applied;

		public boolean isApplied() {
			return Boolean.TRUE.equals(applied);
		}
	}

	public static class History extends BaseResponseHolder {
		@SerializedName("data")
		HistoryData data;

		public HistoryData getData() {
			return data;
		}
	}

	public static class HistoryData {
		Meta meta;
		List<Shift> objects;

		public int getTotalCount() {
			return meta != null && meta.totalCount != null ? meta.totalCount : 0;
		}

		public List<Shift> getObjects() {
			return objects;
		}
	}

	public static class Meta {
		@SerializedName("total_count")
		Integer totalCount;
	}

	public static class Settings extends BaseResponseHolder {
		@SerializedName("data")
		ShiftSettings data;

		public ShiftSettings getData() {
			return data;
		}
	}
}
