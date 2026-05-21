package com.driverspa.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtil {

	/**
	 * Difference in years between two dates. Can be used to calculate age 
	 * @param instant - date to compare to
	 * @return difference in years
	 */
	public static int yearsSince(Instant instant) {
		LocalDate date = instant.atZone(ZoneId.of("UTC")).toLocalDate();
		return LocalDate.now(ZoneId.of("UTC")).getYear() - date.getYear();
	}
	
	public static String getTime(Instant instant) {
		DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()).withZone(ZoneId.of("UTC"));
		return timeFormat.format(instant);
	}
	
}
