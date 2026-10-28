package com.driverspa.admin.fragment;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.shift.ShiftApi;
import com.driverspa.admin.shift.ShiftState;
import com.driverspa.admin.shift.ShiftUi;
import com.driverspa.admin.shift.Shifts;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.model.shift.ShiftSettings;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.squareup.otto.Subscribe;

import java.util.Locale;

/**
 * Shift settings (owner and working directors): require an open shift, the difference that
 * needs a comment, the closing time and the auto-close. There is no Telegram field: the
 * Z-report lives in the app.
 */
public class ShiftSettingsFragment extends AdminBaseFragment {

	private LinearLayout content;
	private Washer washer;
	private User user;
	private boolean userReceived;
	private boolean loaded;
	private String closeTime;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		content = ShiftUi.column(requireContext(), 12);
		content.addView(ShiftUi.value(requireContext(), BA.str(R.string.loading_dots)));
		return ShiftUi.scroll(requireContext(), content);
	}

	@Override
	public void onResume() {
		super.onResume();
		BA.getEventBus().register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		BA.getEventBus().unregister(this);
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		load();
	}

	@Subscribe
	public void onUserReceived(UserGetSelfResponseEvent event) {
		user = event.getUser();
		userReceived = true;
		load();
	}

	private void load() {
		if (loaded || washer == null || !userReceived || content == null || !Shifts.isManager(washer, user)) return;
		loaded = true;
		ShiftApi.settings(washer.getId(), (data, error) -> {
			if (!isAdded() || content == null) return;
			if (data == null) {
				loaded = false;
				content.removeAllViews();
				content.addView(ShiftUi.value(requireContext(), error));
				return;
			}
			render(data);
		});
	}

	private void render(ShiftSettings s) {
		final android.content.Context c = requireContext();
		content.removeAllViews();
		LinearLayout card = ShiftUi.card(c);
		final CheckBox required = ShiftUi.check(c, BA.str(R.string.shift_set_required), s.isRequired());
		card.addView(required);
		card.addView(ShiftUi.hint(c, BA.str(R.string.shift_set_required_hint)));

		card.addView(ShiftUi.label(c, BA.str(R.string.shift_set_limit)));
		final EditText limit = ShiftUi.amountField(c);
		limit.setInputType(InputType.TYPE_CLASS_NUMBER);
		limit.setText(String.valueOf(s.getDiscrepancyLimit()));
		card.addView(limit);
		card.addView(ShiftUi.hint(c, BA.str(R.string.shift_set_limit_hint)));

		closeTime = s.getCloseTime();
		card.addView(ShiftUi.label(c, BA.str(R.string.shift_set_close_time)));
		final TextView time = ShiftUi.button(c, closeTime != null ? closeTime : BA.str(R.string.shift_set_no_time), false);
		card.addView(time);
		card.addView(ShiftUi.hint(c, BA.str(R.string.shift_set_close_time_hint)));
		final CheckBox auto = ShiftUi.check(c, BA.str(R.string.shift_set_auto), s.isAutoClose());
		final TextView afterLabel = ShiftUi.label(c, BA.str(R.string.shift_set_auto_after));
		final EditText after = ShiftUi.amountField(c);
		after.setInputType(InputType.TYPE_CLASS_NUMBER);
		after.setText(String.valueOf(s.getAutoCloseAfter()));
		final TextView clear = ShiftUi.button(c, BA.str(R.string.shift_set_clear_time), false);
		Runnable sync = () -> {
			boolean hasTime = closeTime != null;
			time.setText(hasTime ? closeTime : BA.str(R.string.shift_set_no_time));
			clear.setVisibility(hasTime ? View.VISIBLE : View.GONE);
			auto.setEnabled(hasTime);
			if (!hasTime) auto.setChecked(false);
			int show = hasTime && auto.isChecked() ? View.VISIBLE : View.GONE;
			afterLabel.setVisibility(show);
			after.setVisibility(show);
		};
		time.setOnClickListener(v -> {
			int h = 22, m = 0;
			if (closeTime != null) {
				try {
					h = Integer.parseInt(closeTime.substring(0, 2));
					m = Integer.parseInt(closeTime.substring(3, 5));
				} catch (Exception ignored) {
					// keep the default
				}
			}
			new TimePickerDialog(requireActivity(), (picker, hour, minute) -> {
				closeTime = String.format(Locale.US, "%02d:%02d", hour, minute);
				sync.run();
			}, h, m, true).show();
		});
		clear.setOnClickListener(v -> {
			closeTime = null;
			sync.run();
		});
		auto.setOnCheckedChangeListener((b, checked) -> sync.run());
		card.addView(clear);
		card.addView(auto);
		card.addView(afterLabel);
		card.addView(after);
		sync.run();

		final TextView error = ShiftUi.error(c);
		card.addView(error);
		final TextView save = ShiftUi.button(c, BA.str(R.string.save), true);
		card.addView(save);
		content.addView(card);

		save.setOnClickListener(v -> {
			Integer limitValue = intOf(limit, 1_000_000);
			boolean autoClose = closeTime != null && auto.isChecked();
			Integer afterValue = autoClose ? intOf(after, 1440) : Integer.valueOf(60);
			if (limitValue == null || afterValue == null) {
				ShiftUi.showError(error, BA.str(R.string.shift_err_value));
				return;
			}
			ShiftUi.showError(error, null);
			save.setEnabled(false);
			ShiftApi.saveSettings(washer.getId(), required.isChecked(), limitValue, closeTime, autoClose, afterValue, (data, err) -> {
				save.setEnabled(true);
				if (!isAdded()) return;
				if (data == null) {
					ShiftUi.showError(error, err);
					return;
				}
				ToastUtil.display(getActivity(), BA.str(R.string.shift_settings_saved));
				ShiftState.refresh(washer.getId());
			});
		});
	}

	private static Integer intOf(EditText field, int max) {
		String s = field.getText().toString().trim();
		if (!s.matches("^\\d{1,7}$")) return null;
		int v = Integer.parseInt(s);
		return v <= max ? v : null;
	}
}
