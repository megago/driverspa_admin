package com.driverspa.admin.shift;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.driverspa.BA;
import com.driverspa.R;

/**
 * Views of the shift screens, built in code with the shop's look (dark cards, light-blue
 * labels, white values, bg_v2_* fields and buttons). Works in screens and in dialogs alike.
 */
public final class ShiftUi {

	private ShiftUi() {
	}

	public static int dp(Context c, float dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, c.getResources().getDisplayMetrics());
	}

	private static float px(Context c, int dimen) {
		return c.getResources().getDimension(dimen);
	}

	private static LinearLayout.LayoutParams wide(int top) {
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		lp.topMargin = top;
		return lp;
	}

	/** A vertical column with padding, in a ScrollView. */
	public static ScrollView scroll(Context c, LinearLayout column) {
		ScrollView scroll = new ScrollView(c);
		scroll.setFillViewport(true);
		scroll.addView(column);
		return scroll;
	}

	public static LinearLayout column(Context c, int padDp) {
		LinearLayout l = new LinearLayout(c);
		l.setOrientation(LinearLayout.VERTICAL);
		int pad = dp(c, padDp);
		l.setPadding(pad, pad / 2, pad, pad);
		return l;
	}

	/** A dark card holding a column. */
	public static LinearLayout card(Context c) {
		LinearLayout l = column(c, 12);
		l.setBackgroundResource(R.drawable.bg_v2_card);
		int pad = dp(c, 12);
		l.setPadding(pad, pad, pad, pad);
		l.setLayoutParams(wide(dp(c, 10)));
		return l;
	}

	public static TextView title(Context c, String text) {
		TextView t = text(c, text, R.color.White, R.dimen.text_medium);
		t.setTypeface(t.getTypeface(), Typeface.BOLD);
		t.setLayoutParams(wide(dp(c, 8)));
		return t;
	}

	/** A small light-blue label above a value or a field. */
	public static TextView label(Context c, String text) {
		TextView t = text(c, text, R.color.BlueLight, R.dimen.text_small);
		t.setLayoutParams(wide(dp(c, 12)));
		return t;
	}

	public static TextView value(Context c, String text) {
		TextView t = text(c, text, R.color.White, R.dimen.text_normal);
		t.setLayoutParams(wide(dp(c, 2)));
		return t;
	}

	/** A hint under a field (or an error, with the error colour). */
	public static TextView hint(Context c, String text) {
		TextView t = text(c, text, R.color.BlueLight, R.dimen.text_smaller);
		t.setLayoutParams(wide(dp(c, 4)));
		return t;
	}

	public static TextView error(Context c) {
		TextView t = text(c, "", R.color.OrangeRed, R.dimen.text_small);
		t.setLayoutParams(wide(dp(c, 6)));
		t.setVisibility(View.GONE);
		return t;
	}

	public static void showError(TextView view, String message) {
		view.setText(message);
		view.setVisibility(message == null || message.isEmpty() ? View.GONE : View.VISIBLE);
	}

	public static TextView text(Context c, String text, int color, int size) {
		TextView t = new TextView(c);
		t.setText(text);
		t.setTextColor(ContextCompat.getColor(c, color));
		t.setTextSize(TypedValue.COMPLEX_UNIT_PX, px(c, size));
		return t;
	}

	/** "Label ...... value" on one line. */
	public static LinearLayout row(Context c, String label, String value, int valueColor, boolean bold) {
		LinearLayout row = new LinearLayout(c);
		row.setOrientation(LinearLayout.HORIZONTAL);
		row.setLayoutParams(wide(dp(c, 6)));
		TextView l = text(c, label, R.color.BlueLight, R.dimen.text_small);
		l.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
		TextView v = text(c, value, valueColor, R.dimen.text_small);
		v.setGravity(Gravity.END);
		if (bold) v.setTypeface(v.getTypeface(), Typeface.BOLD);
		row.addView(l);
		row.addView(v);
		return row;
	}

	public static LinearLayout row(Context c, String label, String value) {
		return row(c, label, value, R.color.White, false);
	}

	/** An amount field (digits, one separator). */
	public static EditText amountField(Context c) {
		EditText e = field(c, "0");
		e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		e.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
		return e;
	}

	/** A one-line text field (at most 300 characters, as the backend). */
	public static EditText textField(Context c, String hint) {
		EditText e = field(c, hint);
		e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		e.setFilters(new InputFilter[]{new InputFilter.LengthFilter(300)});
		return e;
	}

	private static EditText field(Context c, String hint) {
		EditText e = new EditText(c);
		e.setBackgroundResource(R.drawable.bg_v2_field);
		int pad = dp(c, 10);
		e.setPadding(pad, pad, pad, pad);
		e.setTextColor(ContextCompat.getColor(c, R.color.White));
		e.setHintTextColor(ContextCompat.getColor(c, R.color.BlueLight));
		e.setTextSize(TypedValue.COMPLEX_UNIT_PX, px(c, R.dimen.text_normal));
		e.setSingleLine(true);
		e.setHint(hint);
		e.setLayoutParams(wide(dp(c, 4)));
		return e;
	}

	public static TextView button(Context c, String text, boolean primary) {
		TextView b = text(c, text, R.color.White, R.dimen.text_normal);
		b.setBackgroundResource(primary ? R.drawable.bg_v2_btn_primary : R.drawable.bg_v2_btn_ghost);
		b.setGravity(Gravity.CENTER);
		b.setTypeface(b.getTypeface(), Typeface.BOLD);
		int padH = dp(c, 14);
		int padV = dp(c, 12);
		b.setPadding(padH, padV, padH, padV);
		b.setClickable(true);
		b.setFocusable(true);
		b.setMinHeight(dp(c, 48));
		b.setLayoutParams(wide(dp(c, 10)));
		return b;
	}

	/** A row of equal buttons. */
	public static LinearLayout buttons(Context c, TextView... items) {
		LinearLayout row = new LinearLayout(c);
		row.setOrientation(LinearLayout.HORIZONTAL);
		// No baseline alignment: a label that wraps to two lines would be pushed down and clipped.
		row.setBaselineAligned(false);
		row.setLayoutParams(wide(dp(c, 10)));
		for (int i = 0; i < items.length; i++) {
			// MATCH_PARENT keeps the buttons equally tall when one label wraps.
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
			if (i > 0) lp.leftMargin = dp(c, 8);
			items[i].setLayoutParams(lp);
			row.addView(items[i]);
		}
		return row;
	}

	public static CheckBox check(Context c, String text, boolean checked) {
		CheckBox box = new CheckBox(c);
		box.setText(text);
		box.setChecked(checked);
		box.setTextColor(ContextCompat.getColor(c, R.color.White));
		box.setTextSize(TypedValue.COMPLEX_UNIT_PX, px(c, R.dimen.text_normal));
		box.setButtonTintList(ContextCompat.getColorStateList(c, R.color.BlueLight));
		box.setMinHeight(dp(c, 44));
		box.setLayoutParams(wide(dp(c, 2)));
		return box;
	}

	public static View divider(Context c) {
		View v = new View(c);
		v.setBackgroundColor(ContextCompat.getColor(c, R.color.BlueTransparent));
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(c, 1));
		lp.topMargin = dp(c, 8);
		v.setLayoutParams(lp);
		return v;
	}

	public static ProgressDialog progress(Activity activity) {
		ProgressDialog progress = new ProgressDialog(activity);
		progress.setTitle("");
		progress.setIndeterminate(true);
		progress.setCancelable(false);
		progress.setMessage(BA.str(R.string.loading_dots));
		return progress;
	}

	/** A dialog with the screens' dark surface (ThemeOverlay.DriverSpa.ShiftDialog). */
	public static MaterialAlertDialogBuilder dialog(Context c) {
		return new MaterialAlertDialogBuilder(c, R.style.ThemeOverlay_DriverSpa_ShiftDialog);
	}

	/** A spinner whose drop-down matches the dark dialogs. */
	public static android.widget.Spinner spinner(Context c) {
		android.widget.Spinner s = new android.widget.Spinner(c);
		s.setPopupBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0xFF1F2A3F));
		s.setBackgroundResource(R.drawable.bg_v2_field);
		s.setMinimumHeight(dp(c, 48));
		s.setLayoutParams(wide(dp(c, 4)));
		return s;
	}

	/** The adapter of spinner(): white items on the dark drop-down. */
	public static android.widget.ArrayAdapter<String> spinnerAdapter(Context c, java.util.List<String> items) {
		return new android.widget.ArrayAdapter<String>(c, android.R.layout.simple_spinner_dropdown_item, items) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				return white(super.getView(position, convertView, parent));
			}

			@Override
			public View getDropDownView(int position, View convertView, ViewGroup parent) {
				return white(super.getDropDownView(position, convertView, parent));
			}

			private View white(View v) {
				if (v instanceof TextView) ((TextView) v).setTextColor(ContextCompat.getColor(c, R.color.White));
				return v;
			}
		};
	}

	public static boolean alive(Activity activity) {
		return activity != null && !activity.isFinishing() && !activity.isDestroyed();
	}
}
