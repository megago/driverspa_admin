package com.driverspa.util.otto.ws;

/**
 * Posted by the FCM service when a {type:"wa_verified"} data push arrives during
 * WhatsApp registration (the user isn't logged in yet, so this fires regardless
 * of admin-login state). The registration screen reacts by polling
 * whatsapp_status immediately instead of waiting for the next timer tick.
 */
public class WhatsappVerifiedPushEvent {
}
