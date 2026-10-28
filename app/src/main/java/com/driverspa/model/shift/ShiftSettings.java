package com.driverspa.model.shift;

import com.google.gson.annotations.SerializedName;

/**
 * Shift settings of a car wash (work_shift/settings). The backend also keeps a Telegram chat
 * (report_chat_id) for managers; the app does not use it: the Z-report lives in the app.
 */
public class ShiftSettings {

	Boolean required;
	@SerializedName("discrepancy_limit")
	Integer discrepancyLimit;
	@SerializedName("close_time")
	String closeTime;
	@SerializedName("auto_close")
	Boolean autoClose;
	@SerializedName("auto_close_after")
	Integer autoCloseAfter;

	/** Payments, shop sales and cash operations need an open shift. */
	public boolean isRequired() {
		return Boolean.TRUE.equals(required);
	}

	/** A larger difference (opening or close) needs a comment. */
	public int getDiscrepancyLimit() {
		return discrepancyLimit != null ? discrepancyLimit : 0;
	}

	/** "HH:MM" in the car wash's zone, or null. */
	public String getCloseTime() {
		return closeTime;
	}

	public boolean isAutoClose() {
		return Boolean.TRUE.equals(autoClose);
	}

	public int getAutoCloseAfter() {
		return autoCloseAfter != null ? autoCloseAfter : 60;
	}
}
