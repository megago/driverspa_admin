package com.driverspa.util;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;

import com.driverspa.BA;

import java.util.Locale;

/**
 * Single source of truth for the app's UI language.
 *
 * <p>The chosen language is persisted in {@link UserPreferences} and applied by wrapping every
 * context — the {@code Application} and each activity — in {@link #wrap(Context)} from their
 * {@code attachBaseContext}. This is fully deterministic across all API levels.
 *
 * <p>Deliberately does NOT use the platform per-app locale
 * ({@code AppCompatDelegate.setApplicationLocales}). Running both at once caused conflicts on
 * API 33+, where the OS-managed application-context locale diverged from the wrapped activity
 * contexts, producing an English-first mix and unreliable switching. Persisted-value +
 * attachBaseContext is the only mechanism.
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

    /** Reads the persisted resource tag from a context, falling back to {@link #DEFAULT}. */
    private static String resolveTag(Context context) {
        try {
            String saved = UserPreferences.getUserLocale(context);
            if (!TextUtils.isEmpty(saved)) {
                for (String tag : SUPPORTED) {
                    if (tag.equals(saved)) return tag;
                }
            }
        } catch (Exception ignored) {
        }
        return DEFAULT;
    }

    /** The persisted resource tag (kk/ru/en), falling back to {@link #DEFAULT}. */
    public static String current() {
        return resolveTag(BA.getContext());
    }

    /**
     * Persists {@code resourceTag} as the app language. The change takes effect once contexts are
     * re-created (see {@code LanguageDialog}, which relaunches the task after calling this).
     */
    public static void apply(String resourceTag) {
        UserPreferences.putUserLocale(BA.getContext(), backendCode(resourceTag));
    }

    /**
     * Ensures a language is persisted on first launch. If the user has never chosen one, the app
     * defaults to Kazakh (rather than following the system locale).
     */
    public static void ensureDefault(Context context) {
        if (!isSupported(UserPreferences.getUserLocale(context))) {
            UserPreferences.putUserLocale(context, DEFAULT);
        }
    }

    private static boolean isSupported(String tag) {
        if (TextUtils.isEmpty(tag)) return false;
        for (String s : SUPPORTED) {
            if (s.equals(tag)) return true;
        }
        return false;
    }

    /**
     * Wraps a base context so its resources resolve in the persisted language. Called from the
     * {@code attachBaseContext} of the Application and every activity, this is what forces the
     * whole UI onto the chosen language regardless of the device's system language.
     */
    public static Context wrap(Context base) {
        Locale locale = new Locale(resolveTag(base));
        Locale.setDefault(locale);
        Configuration config = new Configuration(base.getResources().getConfiguration());
        config.setLocale(locale);
        return base.createConfigurationContext(config);
    }
}
