package com.driverspa.fragment;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.fragment.app.FragmentManager;

import com.driverspa.listener.DatePickerDateSetListener;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.Calendar;

/**
 * Thin wrapper that preserves the legacy DatePickerFragment API
 * (newInstance / setDatePickerDateSetListener / show) but renders the
 * modern MaterialDatePicker calendar UI underneath.
 *
 * Created by Yerzhan Tanatov on 26/04/16.
 */
public class DatePickerFragment {

    private DatePickerDateSetListener listener;
    private final Bundle args = new Bundle();

    public static DatePickerFragment newInstance(String type, int year) {
        DatePickerFragment picker = new DatePickerFragment();
        picker.args.putString("TYPE", type);
        picker.args.putInt("YEAR", year);
        return picker;
    }

    public static DatePickerFragment newInstance(String type, Long date) {
        DatePickerFragment picker = new DatePickerFragment();
        picker.args.putString("TYPE", type);
        picker.args.putLong("DATE", date == null ? -1L : date);
        return picker;
    }

    public static DatePickerFragment newInstance(Long date) {
        DatePickerFragment picker = new DatePickerFragment();
        picker.args.putLong("DATE", date == null ? -1L : date);
        return picker;
    }

    public DatePickerFragment() {
    }

    public void setDatePickerDateSetListener(DatePickerDateSetListener listener) {
        this.listener = listener;
    }

    public void show(FragmentManager fm, String tag) {
        final String type = args.getString("TYPE", "");
        int year = args.getInt("YEAR", 0);
        long date = args.getLong("DATE", -1L);

        long initialSelection;
        if (date < 0) {
            Calendar c = Calendar.getInstance();
            if (year != 0) c.set(Calendar.YEAR, year);
            initialSelection = c.getTimeInMillis();
        } else {
            initialSelection = date;
        }

        CalendarConstraints constraints = buildConstraints(type);

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setSelection(initialSelection)
                .setCalendarConstraints(constraints)
                .build();

        picker.addOnPositiveButtonClickListener(selectedTimestamp -> {
            if (listener != null) {
                listener.onDatePickerDataset(selectedTimestamp, type);
            }
        });

        picker.show(fm, tag);
    }

    private CalendarConstraints buildConstraints(String type) {
        Calendar cal = Calendar.getInstance();
        long minMs;
        long maxMs;

        if (TextUtils.isEmpty(type) || type.equals("start") || type.equals("end")) {
            // Preserve legacy range: 3 years before today .. 3 years after today
            cal.add(Calendar.YEAR, -3);
            minMs = cal.getTimeInMillis();

            cal.add(Calendar.YEAR, 6);
            cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE,      cal.getMaximum(Calendar.MINUTE));
            cal.set(Calendar.SECOND,      cal.getMaximum(Calendar.SECOND));
            cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
            maxMs = cal.getTimeInMillis();
        } else {
            // Preserve legacy range for other types: tomorrow .. 14 days from now
            cal.add(Calendar.DATE, 1);
            minMs = cal.getTimeInMillis();

            cal.add(Calendar.DATE, 13);
            cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE,      cal.getMaximum(Calendar.MINUTE));
            cal.set(Calendar.SECOND,      cal.getMaximum(Calendar.SECOND));
            cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
            maxMs = cal.getTimeInMillis();
        }

        return new CalendarConstraints.Builder()
                .setStart(minMs)
                .setEnd(maxMs)
                .setValidator(DateValidatorPointForward.from(minMs))
                .build();
    }
}
