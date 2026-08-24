package com.driverspa.util;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.driverspa.BA;

import java.util.Locale;

/**
 * Single source of truth for the app's UI language.
 *
 * <p>The chosen language is persisted in {@link UserPreferences} and applied explicitly by
 * wrapping each activity's base context in {@link #wrap(Context)} (called from the base
 * activities' {@code attachBaseContext}). This is deterministic across all API levels and does
 * not rely on AppCompat's per-app-locale auto-store, which on API &lt; 33 could leave activity
 * resources on the system language while code-side {@code getString} used the chosen one —
 * producing a mix. {@link AppCompatDelegate#setApplicationLocales} is still called so it triggers
 * the activity recreation on change and keeps the framework in sync.
 *
 * <p>The backend (washme_be LocaleMiddleware) special-cases {@code kk} for Kazakh and resolves
 * {@code ru}/{@code en} via Django, so the Android resource tag and the Accept-Language code are
 * identical for all three languages.
 */
public final class LocaleManager {

    /** Kazakh — resource tag (values-kk). Default language. */
    public static final String KK = "kk";
    /** Russian — resource tag (base values/). */
    public static final String RU = "ru";
    /** English — resource tag (values-en). */
    public static final String EN = "en";

    /** Default language for fresh installs. */
    public static final String DEFAULT = KK;

    /** Supported languages, in the order they should appear in the picker. */
    public static final String[] SUPPORTED = { KK, RU, EN };

    private LocaleManager() {
    }

    /**
     * Human-readable name of a language, always shown in that language itself
     * (never translated), so users can recognise their own language.
     */
    public static String displayName(String resourceTag) {
        if (KK.equals(resourceTag)) return "Қазақша";
        if (EN.equals(resourceTag)) return "English";
        return "Русский";
    }

    /** The code the backend expects in {@code Accept-Language} (identical to the resource tag). */
    public static String backendCode(String resourceTag) {
        return resourceTag; // kk / ru / en match the backend's Accept-Language codes
    }

    /** The persisted resource tag (kk/ru/en), falling back to {@link #DEFAULT}. */
    public static String current() {
        String saved = UserPreferences.getUserLocale(BA.getContext());
        if (!TextUtils.isEmpty(saved)) {
            for (String tag : SUPPORTED) {
                if (tag.equals(saved)) return tag;
            }
        }
        return DEFAULT;
    }

    /**
     * Applies {@code resourceTag} as the app language: persists it (used by
     * {@link #wrap(Context)} and the request interceptor) and asks AppCompat to recreate the
     * visible activities so they re-read the new locale.
     */
    public static void apply(String resourceTag) {
        UserPreferences.putUserLocale(BA.getContext(), backendCode(resourceTag));
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(resourceTag));
    }

    /**
     * Ensures a language is selected on first launch. If the user has never chosen one,
     * the app defaults to Kazakh (rather than following the system locale).
     */
    public static void ensureDefault(Context context) {
        String saved = UserPreferences.getUserLocale(context);
        boolean valid = false;
        if (!TextUtils.isEmpty(saved)) {
            for (String tag : SUPPORTED) {
                if (tag.equals(saved)) { valid = true; break; }
            }
        }
        if (!valid) {
            UserPreferences.putUserLocale(context, DEFAULT);
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(current()));
    }

    /**
     * Wraps a base context so its resources resolve in the persisted language. Called from the
     * base activities' {@code attachBaseContext}, this is what actually forces every screen onto
     * the chosen language regardless of the device's system language.
     */
    public static Context wrap(Context base) {
        Locale locale = new Locale(current());
        Locale.setDefault(locale);
        Configuration config = new Configuration(base.getResources().getConfiguration());
        config.setLocale(locale);
        return base.createConfigurationContext(config);
    }
}
