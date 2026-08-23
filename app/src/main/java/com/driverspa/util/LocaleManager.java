package com.driverspa.util;
import com.driverspa.R;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.driverspa.BA;

import java.util.Locale;

/**
 * Single source of truth for the app's UI language.
 *
 * <p>Uses the AndroidX per-app language API ({@link AppCompatDelegate#setApplicationLocales})
 * which persists the choice (via the {@code AppLocalesMetadataHolderService} declared in the
 * manifest) and re-applies it to every activity automatically — no per-activity
 * {@code attachBaseContext} boilerplate is required.
 *
 * <p>Note the two different code spaces:
 * <ul>
 *   <li><b>Resource tag</b> — the Android/ISO-639 language tag used for {@code values-*}
 *       folders and {@link Locale}. Kazakh is {@code "kk"}.</li>
 *   <li><b>Backend code</b> — what the API expects in the {@code Accept-Language} header.
 *       The backend uses {@code "kz"} for Kazakh ({@link com.driverspa.Reference#KZ}).</li>
 * </ul>
 * {@link #backendCode(String)} maps between them; {@link #apply(String)} keeps
 * {@link UserPreferences} in sync so the header stays correct.
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
        // Language names are always shown in their own language, never translated.
        if (KK.equals(resourceTag)) return "Қазақша";
        if (EN.equals(resourceTag)) return "English";
        return "Русский";
    }

    /** Maps a resource tag to the code the backend expects in {@code Accept-Language}. */
    public static String backendCode(String resourceTag) {
        if (KK.equals(resourceTag)) return "kz";
        return resourceTag; // ru / en are identical in both spaces
    }

    /**
     * Applies {@code resourceTag} as the app language: switches resources (AppCompat
     * recreates visible activities automatically) and mirrors the backend code into
     * {@link UserPreferences} so network requests send the matching Accept-Language.
     */
    public static void apply(String resourceTag) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(resourceTag));
        UserPreferences.putUserLocale(BA.getContext(), backendCode(resourceTag));
    }

    /** The currently active resource tag (falls back to {@link #DEFAULT}). */
    public static String current() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        if (!locales.isEmpty()) {
            Locale locale = locales.get(0);
            if (locale != null) {
                String lang = locale.getLanguage();
                for (String tag : SUPPORTED) {
                    if (tag.equals(lang)) return tag;
                }
            }
        }
        return DEFAULT;
    }

    /**
     * Ensures a language is selected on first launch. If the user has never chosen one,
     * the app defaults to Kazakh (rather than following the system locale).
     */
    public static void ensureDefault(Context context) {
        if (AppCompatDelegate.getApplicationLocales().isEmpty()) {
            apply(DEFAULT);
        } else {
            // Keep the backend code in sync in case it was cleared.
            UserPreferences.putUserLocale(context, backendCode(current()));
        }
    }
}
