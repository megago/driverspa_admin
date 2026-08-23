package com.driverspa.dialog;

import android.app.Activity;
import android.content.DialogInterface;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.driverspa.R;
import com.driverspa.util.LocaleManager;

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
                            LocaleManager.apply(selected);
                        }
                    }
                })
                .setNegativeButton(R.string.button_ok, null)
                .show();
    }
}
