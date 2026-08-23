package com.driverspa.admin.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.squareup.otto.Subscribe;

import java.text.ParseException;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.dialog.LanguageDialog;
import com.driverspa.model.api.request.AuthAdminRegistrationRequest;
import com.driverspa.model.api.request.CheckPhoneRequest;
import com.driverspa.model.api.request.ResendActivationRequest;
import com.driverspa.util.MaskedWatcher;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AuthAdminRegistrationRequestEvent;
import com.driverspa.util.otto.ws.AuthAdminRegistrationResponseEvent;
import com.driverspa.util.otto.ws.CheckPhoneRequestEvent;
import com.driverspa.util.otto.ws.CheckPhoneResponseEvent;
import com.driverspa.util.otto.ws.ResendActivationRequestEvent;
import com.driverspa.util.otto.ws.ResendActivationResponseEvent;

/**
 * Entry screen of the auth flow. Collects a phone number, email and OTP channel,
 * calls check_phone, then branches. The "Continue" button is enabled only after
 * the user agrees to the public offer (публичная оферта).
 */
public class AdminPhoneFragment extends AdminBaseFragment {

	private static final String OFERTA_URL = "https://driverspa.kz/oferta.html";

	// Local-use flag: when true, skip the check_phone backend call and always
	// force the password screen. Set back to false before shipping.
	private static final boolean LOCAL_FORCE_PASSWORD = false;

	public interface ActivityActions {
		void openPasswordLogin(String phone);
		void openVerification();
		void openWhatsappRegistration(String phone, String email, String companyName);
	}

	private ActivityActions activityActions;
	private String fullPhone;
	private String channel;
	private String email;

	@BindView(R.id.login_phone)
	EditText phone;
	@BindView(R.id.login_email)
	EditText loginEmail;
	@BindView(R.id.emailContainer)
	View emailContainer;
	@BindView(R.id.channelGroup)
	RadioGroup channelGroup;
	@BindView(R.id.ofertaCheckbox)
	CheckBox ofertaCheckbox;
	@BindView(R.id.ofertaLink)
	TextView ofertaLink;
	@BindView(R.id.btnOk)
	Button btnOk;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_admin_phone, container, false);
		ButterKnife.bind(this, view);
		try {
			MaskedWatcher maskedWatcher = new MaskedWatcher("### ###-##-##");
			phone.addTextChangedListener(maskedWatcher);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// WhatsApp is the default/first option. The email field is only relevant
		// for the Email channel, so toggle it with the selection.
		channelGroup.check(R.id.channelWhatsapp);
		channelGroup.setOnCheckedChangeListener((group, checkedId) -> updateEmailVisibility());
		updateEmailVisibility();
		phone.requestFocus();
		phone.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_NEXT) {
					processPhone();
					return true;
				}
				return false;
			}
		});

		// Alpha-only dimming — keeps the button text readable when unchecked.
		// setEnabled is intentionally not used (it triggers Android's gray tint).
		ofertaCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
			btnOk.setAlpha(isChecked ? 1f : 0.35f);
		});

		ofertaLink.setOnClickListener(v -> showOfertaDialog());

		return view;
	}

	/** Full-screen dialog that loads the offer page in a WebView. */
	private void showOfertaDialog() {
		if (getActivity() == null) return;
		Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
		dialog.setContentView(R.layout.dialog_oferta);
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		WebView webView = dialog.findViewById(R.id.ofertaWebView);
		View progress = dialog.findViewById(R.id.ofertaProgress);
		Button btnClose = dialog.findViewById(R.id.ofertaClose);
		Button btnAgree = dialog.findViewById(R.id.ofertaAgree);

		WebSettings ws = webView.getSettings();
		ws.setJavaScriptEnabled(true);
		ws.setDefaultTextEncodingName("utf-8");

		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				if (progress != null) progress.setVisibility(View.GONE);
				webView.setVisibility(View.VISIBLE);
			}
		});
		webView.setVisibility(View.INVISIBLE);
		webView.loadUrl(OFERTA_URL);

		btnClose.setOnClickListener(v -> dialog.dismiss());
		btnAgree.setOnClickListener(v -> {
			ofertaCheckbox.setChecked(true);
			dialog.dismiss();
		});

		dialog.show();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		activityActions = (ActivityActions) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		activityActions = null;
	}

	private String selectedChannel() {
		int id = channelGroup.getCheckedRadioButtonId();
		if (id == R.id.channelWhatsapp) return UserPreferences.CHANNEL_WHATSAPP;
		return UserPreferences.CHANNEL_EMAIL;
	}

	/** Email is only collected for the Email channel; hide it for WhatsApp. */
	private void updateEmailVisibility() {
		boolean isEmail = UserPreferences.CHANNEL_EMAIL.equals(selectedChannel());
		emailContainer.setVisibility(isEmail ? View.VISIBLE : View.GONE);
	}

	@OnClick(R.id.btnLanguage)
	protected void onLanguageClicked() {
		LanguageDialog.show(getActivity());
	}

	@OnClick(R.id.btnOk)
	protected void processPhone() {
		if (!ofertaCheckbox.isChecked()) {
			ToastUtil.display(getActivity(), BA.str(R.string.accept_offer_please));
			return;
		}
		String phoneStr = phone.getText().toString()
				.replaceAll("\\+", "").replaceAll("\\(", "").replaceAll("\\)", "")
				.replaceAll("-", "").replaceAll(" ", "");
		if (TextUtils.isEmpty(phoneStr)) {
			phone.setError(BA.str(R.string.enter_phone));
			return;
		}
		if (phoneStr.length() != 10) {
			phone.setError(BA.str(R.string.invalid_phone));
			return;
		}

		channel = selectedChannel();
		fullPhone = "+7" + phoneStr;
		UserPreferences.putOtpChannel(BA.getContext(), channel);

		// Local-use shortcut: skip check_phone / WhatsApp and always force the
		// password screen, ignoring the selected channel and the backend answer.
		if (LOCAL_FORCE_PASSWORD) {
			activityActions.openPasswordLogin(fullPhone);
			return;
		}

		// WhatsApp registration is a separate flow: it skips check_phone and the
		// OTP code entirely, handing off to AdminWhatsappFragment which calls
		// whatsapp_request and waits for the verification. Email is optional.
		if (UserPreferences.CHANNEL_WHATSAPP.equals(channel)) {
			String emailStr = loginEmail.getText().toString().trim();
			activityActions.openWhatsappRegistration(fullPhone,
					TextUtils.isEmpty(emailStr) ? null : emailStr, null);
			return;
		}

		email = loginEmail.getText().toString().trim();
		if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
			loginEmail.setError(BA.str(R.string.enter_valid_email));
			loginEmail.requestFocus();
			return;
		}

		setWaitScreen(true);
		BA.getEventBus().post(new CheckPhoneRequestEvent(new CheckPhoneRequest(fullPhone)));
	}

	@Subscribe
	public void onCheckPhoneResponse(CheckPhoneResponseEvent event) {
		if (event.getData() == null || event.getData().getResponse() == null
				|| !"success".equals(event.getData().getStatus())) {
			setWaitScreen(false);
			ToastUtil.display(getActivity(), event.getData() != null && event.getData().getMessage() != null
					? event.getData().getMessage() : BA.str(R.string.err_try_again));
			return;
		}

		if (event.getData().getResponse().isHasPassword()) {
			setWaitScreen(false);
			activityActions.openPasswordLogin(fullPhone);
		} else if (!event.getData().getResponse().isExists()) {
			String emailToSend = TextUtils.isEmpty(email) ? null : email;
			BA.getEventBus().post(new AuthAdminRegistrationRequestEvent(
					new AuthAdminRegistrationRequest("NA", fullPhone, channel, emailToSend)));
		} else {
			BA.getEventBus().post(new ResendActivationRequestEvent(
					new ResendActivationRequest(fullPhone, channel)));
		}
	}

	@Subscribe
	public void onRegistrationResponse(AuthAdminRegistrationResponseEvent event) {
		setWaitScreen(false);
		if (event.getAuthLoginResponse() != null
				&& event.getAuthLoginResponse().getStatus() != null
				&& !event.getAuthLoginResponse().getStatus().equals("error")) {
			activityActions.openVerification();
		} else {
			ToastUtil.display(getActivity(), BA.str(R.string.err_try_again));
		}
	}

	@Subscribe
	public void onResendActivationResponse(ResendActivationResponseEvent event) {
		setWaitScreen(false);
		if (event.getData() != null && "success".equals(event.getData().getStatus())) {
			activityActions.openVerification();
		} else {
			ToastUtil.display(getActivity(), event.getData() != null && event.getData().getMessage() != null
					? event.getData().getMessage() : BA.str(R.string.err_try_again));
		}
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
}
