package com.driverspa.assist;

import android.text.TextUtils;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import com.driverspa.BA;
import com.driverspa.model.User;
import com.driverspa.model.api.response.WhatsappRequestResponseHolder;
import com.driverspa.model.api.response.WhatsappStatusResponseHolder;
import com.driverspa.util.RetrofitClient.DriverSpaApi;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AdminBootstrapRequestEvent;
import com.driverspa.util.otto.ws.AuthAdminVerificationResponseEvent;
import com.driverspa.util.otto.ws.WhatsappRequestEvent;
import com.driverspa.util.otto.ws.WhatsappRequestResponseEvent;
import com.driverspa.util.otto.ws.WhatsappStatusRequestEvent;
import com.driverspa.util.otto.ws.WhatsappStatusResponseEvent;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Drives the two-call WhatsApp registration flow:
 *
 *  - whatsapp_request: ask the backend for a short token + wa_link; the screen
 *    deep-links the user into WhatsApp with the token pre-filled.
 *  - whatsapp_status: poll with that SHORT token. While pending the backend
 *    returns {verified:false}; once the WhatsApp message (sent from the same
 *    number) is matched it returns the full account + password_required + token,
 *    where token is the api_key.
 *
 * On verification this signs the admin in (stores the api_key) and then reuses
 * the existing post-sign-in flow: AdminBootstrapRequestEvent (handled by
 * {@link AuthVerificationAssist}) for ready accounts, or an
 * {@link AuthAdminVerificationResponseEvent} with passwordRequired for the
 * new-password flow.
 */
public class WhatsappAuthAssist extends BaseAssist {

    // Guards against the polling loop firing the sign-in/bootstrap twice when a
    // verified status is returned by more than one in-flight poll. Reset on each
    // fresh whatsapp_request (incl. Retry).
    private volatile boolean verifiedHandled;

    public WhatsappAuthAssist(DriverSpaApi api, Bus eventBus) {
        super(api, eventBus);
    }

    @Subscribe
    public void onWhatsappRequested(final WhatsappRequestEvent event) {
        logD("onWhatsappRequested");
        verifiedHandled = false;
        getApi().whatsappRequest(event.getRequest(), new Callback<WhatsappRequestResponseHolder>() {
            @Override
            public void success(WhatsappRequestResponseHolder data, Response response) {
                if (isSuccess(data) && data.getResponse() != null) {
                    getEventsBus().post(new WhatsappRequestResponseEvent(data));
                } else {
                    onDataError(data);
                    getEventsBus().post(new WhatsappRequestResponseEvent(data));
                }
            }

            @Override
            public void failure(RetrofitError error) {
                onRetrofitError(error);
                getEventsBus().post(new WhatsappRequestResponseEvent(new WhatsappRequestResponseHolder()));
            }
        });
    }

    @Subscribe
    public void onWhatsappStatusRequested(final WhatsappStatusRequestEvent event) {
        logD("onWhatsappStatusRequested");
        getApi().whatsappStatus(event.getRequest(), new Callback<WhatsappStatusResponseHolder>() {
            @Override
            public void success(WhatsappStatusResponseHolder data, Response response) {
                User user = data.getResponse();
                // The presence of an api_key token is the unambiguous "done"
                // signal; while pending the backend returns {verified:false}.
                if (isSuccess(data) && user != null && !TextUtils.isEmpty(user.getToken())) {
                    if (verifiedHandled) {
                        // Another in-flight poll already signed us in — ignore.
                        return;
                    }
                    verifiedHandled = true;
                    UserPreferences.onAdminLogin(BA.getContext(), user.getId(), user.getPhone(), user.getToken());
                    writeSelfToDb(user);
                    if (user.isPasswordRequired()) {
                        // New-password flow: stop here and let the UI collect a
                        // password (carwash bootstrap happens after set_password).
                        getEventsBus().post(buildPasswordRequiredEvent());
                    } else {
                        // Reuse the existing carwash bootstrap (fetch/create carwash,
                        // then post AuthAdminVerificationResponseEvent).
                        getEventsBus().post(new AdminBootstrapRequestEvent());
                    }
                } else {
                    // Still pending (or a soft error) — let the screen keep polling
                    // until its timeout fires.
                    getEventsBus().post(new WhatsappStatusResponseEvent(data));
                }
            }

            @Override
            public void failure(RetrofitError error) {
                // Network hiccup mid-poll: don't surface a hard error, the screen
                // will retry on its next tick.
                onRetrofitError(error, false);
                getEventsBus().post(new WhatsappStatusResponseEvent(null));
            }
        });
    }

    /**
     * Builds an {@link AuthAdminVerificationResponseEvent} flagged passwordRequired
     * with a success status, mirroring the activate (OTP) new-password branch so the
     * registration screen routes to the create-password step.
     */
    private AuthAdminVerificationResponseEvent buildPasswordRequiredEvent() {
        com.driverspa.model.api.response.AuthAdminVerificationResponseHolder holder =
                new com.driverspa.model.api.response.AuthAdminVerificationResponseHolder();
        holder.setStatus("success");
        AuthAdminVerificationResponseEvent ev = new AuthAdminVerificationResponseEvent(holder);
        ev.setPasswordRequired(true);
        return ev;
    }
}
