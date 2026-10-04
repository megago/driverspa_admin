package com.driverspa.admin.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.driverspa.BA;
import com.driverspa.activity.CityChooseActivity;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AdminBootstrapRequestEvent;
import com.driverspa.util.otto.ws.AuthAdminVerificationResponseEvent;

/**
 * Base for the sign-in screens. The carwash bootstrap needs a city to create the
 * initial carwash; when the account is signed in without one (password login,
 * reset, OTP or WhatsApp for a number first registered in the client app) these
 * screens open the city picker and re-run the bootstrap once it returns.
 */
public abstract class AdminAuthBaseFragment extends AdminBaseFragment {

	private static final String STATE_AWAITING_CITY = "STATE_AWAITING_CITY";

	private boolean awaitingCity = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			awaitingCity = savedInstanceState.getBoolean(STATE_AWAITING_CITY, false);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_AWAITING_CITY, awaitingCity);
	}

	/**
	 * Opens the city picker; the bootstrap is re-posted from onResume once a
	 * city is stored.
	 */
	protected void chooseCityThenBootstrap() {
		awaitingCity = true;
		startActivity(new Intent(getActivity(), CityChooseActivity.class));
	}

	/**
	 * Handles a bootstrap response that asks for a city first.
	 * @return true if the event was consumed and the caller should not route
	 */
	protected boolean handleCityRequired(AuthAdminVerificationResponseEvent event) {
		if (!event.isCityRequired())
			return false;
		chooseCityThenBootstrap();
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		// Returning from the city picker: kick off the carwash bootstrap.
		if (awaitingCity && !TextUtils.isEmpty(UserPreferences.getCity(BA.getContext()))) {
			awaitingCity = false;
			setWaitScreen(true);
			BA.getEventBus().post(new AdminBootstrapRequestEvent());
		}
	}
}
