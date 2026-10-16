package com.driverspa.fcm;

import static com.driverspa.util.Constants.EXTRA_BOOKING_ID;
import static com.driverspa.util.Constants.EXTRA_PUSH_MESSAGE;
import static com.driverspa.util.Constants.EXTRA_PUSH_TYPE;
import static com.driverspa.util.Constants.EXTRA_WASHER_ID;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.activity.AdminHomeActivity;
import com.driverspa.assist.UserSelfAssist;
import com.driverspa.model.Device;
import com.driverspa.model.NotificationId;
import com.driverspa.model.PushData;
import com.driverspa.model.User;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.PushRequestEvent;
import com.driverspa.util.otto.ws.WhatsappVerifiedPushEvent;
import com.driverspa.util.otto.NewBookingPushRequestEvent;
import com.driverspa.util.otto.NewInformationPushRequestEvent;
import com.driverspa.util.otto.NewReviewPushRequestEvent;
import com.driverspa.util.otto.PushUpdateListRequestEvent;
import com.driverspa.util.otto.RemoveBookingPushRequestEvent;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Replaces the old GCM listener service. Receives FCM data messages,
 * deserializes the JSON "message" field into a PushData, posts the
 * matching Otto event so any currently-foreground fragments can react
 * in-process, and posts a system notification that opens
 * AdminHomeActivity routed by push type when tapped.
 *
 * Created by Yerzhan Tanatov on 08.01.2025.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM";
    public static final String DEFAULT_CHANNEL_ID = "default";

    private static final String TYPE_WA_VERIFIED = "wa_verified";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();
        Log.d(TAG, "Push received from=" + remoteMessage.getFrom() + " data=" + data);

        // WhatsApp verification arrives BEFORE the admin is logged in (the whole
        // point is to finish registration), so this must run ahead of the
        // admin-login gate below — otherwise it'd be dropped as "unauthenticated".
        if (TYPE_WA_VERIFIED.equals(extractType(data))) {
            BA.getEventBus().post(new WhatsappVerifiedPushEvent());
            return;
        }

        // This is the admin app — admins are signed in via onAdminLogin, which
        // sets the ADMIN flag (not the client "user" flag). Gating on
        // isUserLoggedIn() here would silently drop every push for admins.
        if (!UserPreferences.isAdminLoggedIn(this)) {
            // Without a signed-in admin we can't route anywhere useful;
            // drop the push silently rather than waking an unauthenticated UI.
            return;
        }

        // Backend sends the actual payload in a "message" string (legacy
        // GCM contract) — keep parsing that for backwards compatibility,
        // but also accept individual fields directly in the data map if
        // the backend ever switches to native FCM data messages.
        String messageJson = data.get("message");
        PushData pushData;
        if (!TextUtils.isEmpty(messageJson)) {
            try {
                pushData = PushData.deserialize(messageJson);
            } catch (Exception e) {
                Log.w(TAG, "Failed to parse push 'message' as JSON, falling back to raw data map", e);
                pushData = pushDataFromMap(data);
            }
        } else {
            pushData = pushDataFromMap(data);
        }

        if (pushData == null || TextUtils.isEmpty(pushData.getType())) {
            Log.w(TAG, "Push dropped: no usable type");
            return;
        }

        dispatchEvent(pushData);
        sendNotification(pushData);
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "FCM token refreshed: " + token);
        Context ctx = getApplicationContext();
        UserPreferences.putPushToken(ctx, token);
        // Register the device with the backend as soon as the token is known.
        // This is the most reliable moment (independent of any Activity's
        // lifecycle / Play-Services timing), so the server always ends up with
        // a token-bearing device record.
        registerDeviceWithBackend(ctx, token);
    }

    /**
     * Posts the device (with its FCM token) to the backend, but only when an
     * admin is signed in. Mirrors the payload built in SplashActivity.
     */
    private void registerDeviceWithBackend(Context ctx, String token) {
        if (TextUtils.isEmpty(token) || !UserPreferences.isAdminLoggedIn(ctx)) {
            return;
        }
        User user = UserSelfAssist.getUserFromDb(UserPreferences.getUserId(ctx));
        if (user == null) {
            return;
        }
        String deviceId = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
        String appVersion = "";
        try {
            appVersion = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        BA.getEventBus().post(new PushRequestEvent(deviceId,
                new Device(Build.MODEL, "android", Build.VERSION.RELEASE, appVersion, token, "fcm",
                        user.getDefaultCarwashId(), true, !user.isOwner())));
    }

    /**
     * Returns the push "type" regardless of whether the backend put it directly
     * in the data map (native FCM data message) or nested inside the legacy
     * "message" JSON string. Used to spot {@code wa_verified} before the
     * admin-login gate.
     */
    private String extractType(Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        String type = data.get("type");
        if (!TextUtils.isEmpty(type)) {
            return type;
        }
        String messageJson = data.get("message");
        if (!TextUtils.isEmpty(messageJson)) {
            try {
                PushData pd = PushData.deserialize(messageJson);
                return pd == null ? null : pd.getType();
            } catch (Exception e) {
                Log.w(TAG, "extractType: failed to parse 'message' JSON", e);
            }
        }
        return null;
    }

    private PushData pushDataFromMap(Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        PushData pd = new PushData(data.get("object_id"));
        pd.setType(data.get("type"));
        pd.setText(data.get("text"));
        pd.setCarwashId(data.get("carwash_id"));
        return pd;
    }

    /**
     * Mirror the dispatch contract used previously by the GCM listener so
     * foreground fragments keep their in-process reactions.
     */
    private void dispatchEvent(PushData pushData) {
        String type = pushData.getType();
        try {
            // Cancel/removed first: "booking_canceled" also contains "book" and
            // must clear the unread badge, not add to it.
            if (type.contains("cancel") || type.contains("removed")) {
                BA.getEventBus().post(new RemoveBookingPushRequestEvent(pushData));
                BA.getEventBus().post(new PushUpdateListRequestEvent());
            } else if (type.contains("book") || type.contains("serving") || type.contains("express")) {
                BA.getEventBus().post(new NewBookingPushRequestEvent(pushData));
                // Reload the open bookings list / timetable so the change shows
                // up without a manual refresh.
                BA.getEventBus().post(new PushUpdateListRequestEvent());
            } else if (type.contains("review")) {
                BA.getEventBus().post(new NewReviewPushRequestEvent(pushData));
            } else if (type.contains("info")) {
                BA.getEventBus().post(new NewInformationPushRequestEvent(pushData));
            }
        } catch (Exception e) {
            // Otto can fan-out exceptions from handlers; don't let one
            // dead subscriber kill the whole push path.
            Log.w(TAG, "Otto dispatch failed for type=" + type, e);
        }
    }

    private void sendNotification(PushData pushData) {
        Intent intent = new Intent(getApplicationContext(), AdminHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_PUSH_TYPE, pushData.getType());
        intent.putExtra(EXTRA_PUSH_MESSAGE, pushData.getText());
        intent.putExtra(EXTRA_BOOKING_ID, pushData.getObjectId());
        intent.putExtra(EXTRA_WASHER_ID, pushData.getObjectId());

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, NotificationId.getID(), intent, flags);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String text = pushData.getText() == null ? "" : pushData.getText();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("DriverSpa")
                .setContentText(text)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text));

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(NotificationId.getID(), builder.build());
        }
    }
}
