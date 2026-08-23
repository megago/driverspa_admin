package com.driverspa.admin.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.otto.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.assist.BaseAssist;
import com.driverspa.model.api.request.WhatsappRequest;
import com.driverspa.model.api.request.WhatsappStatusRequest;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AuthAdminVerificationResponseEvent;
import com.driverspa.util.otto.ws.WhatsappRequestEvent;
import com.driverspa.util.otto.ws.WhatsappRequestResponseEvent;
import com.driverspa.util.otto.ws.WhatsappStatusRequestEvent;
import com.driverspa.util.otto.ws.WhatsappStatusResponseEvent;
import com.driverspa.util.otto.ws.WhatsappVerifiedPushEvent;

/**
 * WhatsApp registration screen. Requests a short token + wa_link, deep-links the
 * user into WhatsApp (where they tap send — Android can't auto-send WhatsApp),
 * then waits for the result via either an FCM {type:"wa_verified"} push or a
 * poll of whatsapp_status every {@link #POLL_INTERVAL_MS}. The match is made by
 * the backend comparing the SENDER number to the number being registered, so the
 * user must have WhatsApp on that SIM; otherwise the status stays unverified and
 * after {@link #TIMEOUT_MS} we offer a Retry.
 */
public class AdminWhatsappFragment extends AdminBaseFragment {

    private static final String TAG = "WhatsappFragment";

    public static final String ARG_PHONE = "ARG_PHONE";
    public static final String ARG_EMAIL = "ARG_EMAIL";
    public static final String ARG_COMPANY = "ARG_COMPANY";

    private static final long POLL_INTERVAL_MS = 3_000L;
    // ~2.5 min — the spec asks for a 2–3 min timeout before offering Retry.
    private static final long TIMEOUT_MS = 150_000L;

    // Reuses MainActivity's existing post-sign-in navigation (shared with the
    // OTP verification screen) so the WhatsApp path lands in the same places.
    private AdminVerificationFragment.ActivityActions activityActions;

    @BindView(R.id.btnSendWhatsapp)
    Button sendButton;
    @BindView(R.id.waitLayout)
    View waitLayout;
    @BindView(R.id.txtTimeout)
    TextView txtTimeout;
    @BindView(R.id.btnRetry)
    Button retryButton;

    private String phone;
    private String email;
    private String companyName;

    private String waToken;        // short WhatsApp token (poll / send with this)
    private String waLink;         // raw link from the backend (text = token only)
    private String waMessageLink;  // link we actually open: token wrapped in instructions
    private String whatsappNumber;

    private boolean requestInFlight;  // whatsapp_request has been sent, awaiting response
    private boolean waiting;          // polling for verification
    private long pollStartTime;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!waiting) {
                return;
            }
            if (System.currentTimeMillis() - pollStartTime >= TIMEOUT_MS) {
                onTimeout();
                return;
            }
            requestStatus();
            handler.postDelayed(this, POLL_INTERVAL_MS);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_whatsapp, container, false);
        ButterKnife.bind(this, view);

        Bundle args = getArguments();
        if (args != null) {
            phone = args.getString(ARG_PHONE);
            email = args.getString(ARG_EMAIL);
            companyName = args.getString(ARG_COMPANY);
        }
        // Disabled until whatsapp_request returns a wa_link to open.
        sendButton.setAlpha(0.35f);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activityActions = (AdminVerificationFragment.ActivityActions) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activityActions = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        BA.getEventBus().register(this);
        if (!requestInFlight && TextUtils.isEmpty(waToken)) {
            startWhatsappRequest();
        } else if (waiting) {
            // Returning to the screen mid-wait — resume polling.
            handler.removeCallbacks(pollRunnable);
            handler.post(pollRunnable);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        BA.getEventBus().unregister(this);
        handler.removeCallbacks(pollRunnable);
    }

    /**
     * Kicks off whatsapp_request. Needs the device FCM token as push_token (the
     * user isn't logged in yet, so a push is the only way to notify them).
     */
    private void startWhatsappRequest() {
        requestInFlight = true;
        setWaitScreen(true);
        txtTimeout.setVisibility(View.GONE);
        retryButton.setVisibility(View.GONE);

        String cached = UserPreferences.getPushToken(BA.getContext());
        if (!TextUtils.isEmpty(cached)) {
            postRequest(cached);
            return;
        }
        // No cached token yet — fetch one, then fire the request.
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            String token = task.isSuccessful() ? task.getResult() : null;
            if (!TextUtils.isEmpty(token)) {
                UserPreferences.putPushToken(BA.getContext(), token);
            } else {
                Log.w(TAG, "FCM getToken failed", task.getException());
            }
            postRequest(token);
        });
    }

    private void postRequest(String pushToken) {
        if (!isAdded()) {
            return;
        }
        BA.getEventBus().post(new WhatsappRequestEvent(
                new WhatsappRequest(phone, pushToken,
                        TextUtils.isEmpty(email) ? null : email,
                        TextUtils.isEmpty(companyName) ? "NA" : companyName)));
    }

    @Subscribe
    public void onWhatsappRequestResponse(WhatsappRequestResponseEvent event) {
        requestInFlight = false;
        setWaitScreen(false);
        if (event.getData() != null && event.getData().getResponse() != null
                && !TextUtils.isEmpty(event.getData().getResponse().getWaLink())) {
            waToken = event.getData().getResponse().getToken();
            waLink = event.getData().getResponse().getWaLink();
            whatsappNumber = event.getData().getResponse().getWhatsappNumber();
            waMessageLink = buildMessageLink();

            // Emulator helper: WhatsApp usually isn't installed, so log everything
            // needed to verify by hand. The "message" is what must be sent to the
            // number from the SAME phone being registered.
            Log.i(TAG, "=== WhatsApp registration ===");
            Log.i(TAG, "Send to number : +" + extractNumber());
            Log.i(TAG, "Message        : " + buildMessageText());
            Log.i(TAG, "wa_link (open) : " + waMessageLink);
            Log.i(TAG, "Poll token     : " + waToken);
            Log.i(TAG, "=============================");

            sendButton.setAlpha(1f);
            // Open WhatsApp straight away so the user only has to tap "send".
            openWhatsapp();
            startWaiting();
        } else {
            ToastUtil.display(getActivity(), BA.str(R.string.err_try_again));
            txtTimeout.setText(BA.str(R.string.err_whatsapp_start));
            txtTimeout.setVisibility(View.VISIBLE);
            retryButton.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.btnSendWhatsapp)
    protected void onSendClicked() {
        if (TextUtils.isEmpty(waLink)) {
            return;
        }
        openWhatsapp();
        if (!waiting) {
            startWaiting();
        }
    }

    /** Opens the chat with the full instruction message + token pre-filled. */
    private void openWhatsapp() {
        String link = TextUtils.isEmpty(waMessageLink) ? waLink : waMessageLink;
        if (TextUtils.isEmpty(link)) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        intent.setPackage("com.whatsapp");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException notInstalled) {
            // WhatsApp not installed — fall back to the browser (wa.me handles it).
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
            } catch (ActivityNotFoundException e) {
                ToastUtil.display(getActivity(), BA.str(R.string.install_whatsapp));
            }
        }
    }

    /** The message body the user sends: instructions wrapping the token. */
    private String buildMessageText() {
        return BA.str(R.string.whatsapp_message_template, waToken);
    }

    /** Digits of the destination number, from the backend field or the wa_link. */
    private String extractNumber() {
        if (!TextUtils.isEmpty(whatsappNumber)) {
            return whatsappNumber.replaceAll("[^0-9]", "");
        }
        try {
            String seg = Uri.parse(waLink).getLastPathSegment();
            return seg == null ? "" : seg.replaceAll("[^0-9]", "");
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Builds a wa.me link whose pre-filled text is the full instruction message
     * (not just the bare token returned by the backend). Uri.Builder encodes the
     * body correctly (spaces as %20, newlines as %0A).
     */
    private String buildMessageLink() {
        String number = extractNumber();
        if (TextUtils.isEmpty(number)) {
            return waLink;
        }
        return new Uri.Builder()
                .scheme("https")
                .authority("wa.me")
                .appendPath(number)
                .appendQueryParameter("text", buildMessageText())
                .build()
                .toString();
    }

    private void startWaiting() {
        waiting = true;
        pollStartTime = System.currentTimeMillis();
        waitLayout.setVisibility(View.VISIBLE);
        txtTimeout.setVisibility(View.GONE);
        retryButton.setVisibility(View.GONE);
        handler.removeCallbacks(pollRunnable);
        handler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
    }

    private void stopWaiting() {
        waiting = false;
        handler.removeCallbacks(pollRunnable);
        waitLayout.setVisibility(View.GONE);
    }

    private void requestStatus() {
        if (TextUtils.isEmpty(waToken)) {
            return;
        }
        BA.getEventBus().post(new WhatsappStatusRequestEvent(new WhatsappStatusRequest(waToken)));
    }

    private void onTimeout() {
        stopWaiting();
        txtTimeout.setText(BA.str(R.string.whatsapp_no_message) + phone
                + BA.str(R.string.whatsapp_check_sim));
        txtTimeout.setVisibility(View.VISIBLE);
        retryButton.setVisibility(View.VISIBLE);
    }

    /** An FCM push says verification succeeded — check status immediately. */
    @Subscribe
    public void onWhatsappVerifiedPush(WhatsappVerifiedPushEvent event) {
        if (waiting) {
            handler.removeCallbacks(pollRunnable);
            requestStatus();
            handler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
        } else if (!TextUtils.isEmpty(waToken)) {
            requestStatus();
        }
    }

    /** Fired for each poll that is still pending; the verified case arrives as
     * an {@link AuthAdminVerificationResponseEvent} instead. Nothing to do here —
     * the timer drives the loop. */
    @Subscribe
    public void onWhatsappStatusResponse(WhatsappStatusResponseEvent event) {
        // no-op: still waiting
    }

    @OnClick(R.id.btnRetry)
    protected void onRetryClicked() {
        waToken = null;
        waLink = null;
        waMessageLink = null;
        whatsappNumber = null;
        startWhatsappRequest();
    }

    /**
     * Verification succeeded and the admin is now signed in (api_key stored).
     * Routes exactly like the OTP verification screen.
     */
    @Subscribe
    public void onAuthVerificationResponseReceived(AuthAdminVerificationResponseEvent event) {
        stopWaiting();
        setWaitScreen(false);
        if (event.getAuthLoginResponse() != null) {
            if (BaseAssist.isSuccess(event.getAuthLoginResponse())) {
                if (event.isPasswordRequired()) {
                    activityActions.openCreatePassword();
                } else if (event.isActiveCarwash()) {
                    activityActions.openAdminHomeActivity();
                } else {
                    activityActions.openWizardActivity();
                }
            } else {
                ToastUtil.display(getActivity(), event.getAuthLoginResponse().getMessage());
            }
        } else {
            ToastUtil.display(getActivity(), BA.str(R.string.err_try_again));
        }
    }
}
