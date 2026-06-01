package com.driverspa.util.otto.ws;

/**
 * Triggers the post-sign-in admin bootstrap (fetch/create carwash, then post
 * AuthAdminVerificationResponseEvent). Used after the new-user "create password"
 * + city selection step, where the account is already signed in (token stored)
 * but the carwash has not been provisioned yet.
 */
public class AdminBootstrapRequestEvent {
}
