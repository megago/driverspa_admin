package com.driverspa.util;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.junit.Test;

import java.time.Instant;
import java.util.Calendar;
import java.util.TimeZone;

public class ConvertersDateParsingTest {

    static class QueuePayload {
        Instant time;

        @SerializedName("end_time")
        Instant endTime;
    }

    @Test
    public void parsesIso8601UtcOffsetWithoutApplyingLocalOffsetTwice() {
        TimeZone previous = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Qyzylorda"));
            Gson gson = Converters.gsonWithDate();

            QueuePayload payload = gson.fromJson(
                    "{\"time\":\"2026-05-21T00:23:29+00:00\",\"end_time\":\"2026-05-21T00:53:29+00:00\"}",
                    QueuePayload.class
            );

            assertEquals(Instant.parse("2026-05-21T00:23:29Z"), payload.time);
            assertEquals(Instant.parse("2026-05-21T00:53:29Z"), payload.endTime);
        } finally {
            TimeZone.setDefault(previous);
        }
    }

    @Test
    public void queuedCountdownDifferenceIsSixMinutesNotThreeHundredAndSix() {
        TimeZone previous = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Qyzylorda"));
            Gson gson = Converters.gsonWithDate();

            QueuePayload payload = gson.fromJson(
                    "{\"end_time\":\"2026-05-21T00:53:29+00:00\"}",
                    QueuePayload.class
            );

            long currentTime = Instant.parse("2026-05-21T00:47:29Z").toEpochMilli();
            long timeDiff = payload.endTime.toEpochMilli() - currentTime;
            long minutes = timeDiff / (1000 * 60);

            assertEquals(6L, minutes);
        } finally {
            TimeZone.setDefault(previous);
        }
    }

    @Test
    public void startAndEndOfDayUseJavaTimeBoundaries() {
        TimeZone previous = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Qyzylorda"));

            Instant sample = Instant.parse("2026-05-21T12:34:56Z");
            Instant start = Functions.getStartOfDay(sample);
            Instant end = Functions.getEndOfDay(sample);

            Calendar startCalendar = Calendar.getInstance();
			startCalendar.setTimeInMillis(start.toEpochMilli());
            Calendar endCalendar = Calendar.getInstance();
			endCalendar.setTimeInMillis(end.toEpochMilli());

            assertEquals(0, startCalendar.get(Calendar.HOUR_OF_DAY));
            assertEquals(0, startCalendar.get(Calendar.MINUTE));
            assertEquals(23, endCalendar.get(Calendar.HOUR_OF_DAY));
            assertEquals(59, endCalendar.get(Calendar.MINUTE));
        } finally {
            TimeZone.setDefault(previous);
        }
    }
}

