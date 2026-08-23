package com.driverspa.dialog;

import android.app.Activity;
import android.content.DialogInterface;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.util.LocaleManager;
import com.driverspa.util.otto.ws.AboutUSRequestEvent;
import com.driverspa.util.otto.ws.InitRequestEvent;

/**
 * Simple single-choice language picker. Applying a choice switches the app UI
 * language via {@link LocaleManager} (AppCompat recreates the visible activity
 * automatically to render the new locale).
 */
public final class LanguageDialog {

    private LanguageDialog() {
    }

    public static void show(final Activity activity) {
        final String[] tags = LocaleManager.SUPPORTED;
        final CharSequence[] names = new CharSequence[tags.length];
        int checked = 0;
        String current = LocaleManager.current();
        for (int i = 0; i < tags.length; i++) {
            names[i] = LocaleManager.displayName(tags[i]);
            if (tags[i].equals(current)) checked = i;
        }

        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.settings_language)
                .setSingleChoiceItems(names, checked, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        String selected = tags[which];
                        if (!selected.equals(LocaleManager.current())) {
                            // Apply first so the stored language (used by the request
                            // interceptor) is updated before we refresh, then re-fetch
                            // the server-provided reference dictionary (roles, statuses,
                            // car/payment types, etc.) so it comes back localized.
                            LocaleManager.apply(selected);
                            BA.getEventBus().post(new InitRequestEvent(LocaleManager.backendCode(selected)));
                            BA.getEventBus().post(new AboutUSRequestEvent());
                        }
                    }
                })
                .setNegativeButton(R.string.button_ok, null)
                .show();
    }
}
