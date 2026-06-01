package com.driverspa.util;

import android.text.TextUtils;

/**
 * Client-side mirror of the server password rules (server is the source of
 * truth). Returns a localized error message, or null when the password is valid.
 */
public class PasswordUtil {

    public static String validate(String password, String passwordConfirm) {
        if (TextUtils.isEmpty(password) || password.length() < 8) {
            return "Пароль должен содержать минимум 8 символов";
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isLetter(c)) hasLetter = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }
        if (!hasLetter) {
            return "Пароль должен содержать хотя бы одну букву";
        }
        if (!hasDigit) {
            return "Пароль должен содержать хотя бы одну цифру";
        }
        if (!password.equals(passwordConfirm)) {
            return "Пароли не совпадают";
        }
        return null;
    }
}
