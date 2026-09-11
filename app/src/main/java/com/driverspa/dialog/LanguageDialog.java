package com.driverspa.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.driverspa.R;
import com.driverspa.util.LocaleManager;

/**
 * Simple single-choice language picker. Applying a choice persists the language and fully
 * restarts the app process so every context rebuilds in the new locale.
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
                            restartApp(activity);
                        }
                    }
                })
                .setNegativeButton(R.string.button_ok, null)
                .show();
    }

    /**
     * Fully restarts the app process so every context (including the Application) is rebuilt in
     * the newly chosen language, and the splash re-fetches the reference dictionary in it. A
     * process restart is required because {@code Application.attachBaseContext} — where the locale
     * is applied — only runs once per process.
     */
    private static void restartApp(Activity activity) {
        Intent launch = activity.getPackageManager()
                .getLaunchIntentForPackage(activity.getPackageName());
        if (launch != null && launch.getComponent() != null) {
            activity.startActivity(Intent.makeRestartActivityTask(launch.getComponent()));
        }
        Runtime.getRuntime().exit(0);
    }
}
