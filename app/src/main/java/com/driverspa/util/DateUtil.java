package com.driverspa.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtil {

	/**
	 * Difference in years between two dates. Can be used to calculate age 
	 * @param date - date to compare to
	 * @return
	 */
	public static int yearsSince(Instant instant) {
		LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();
		return LocalDate.now(ZoneId.systemDefault()).getYear() - date.getYear();
	}
	
	public static String getTime(Instant instant) {
		DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()).withZone(ZoneId.systemDefault());
		return timeFormat.format(instant);
	}
	
}
