package com.driverspa.util;
import com.driverspa.R;
import com.driverspa.BA;

import android.text.TextUtils;

/**
 * Client-side mirror of the server password rules (server is the source of
 * truth). Returns a localized error message, or null when the password is valid.
 */
public class PasswordUtil {

    public static String validate(String password, String passwordConfirm) {
        if (TextUtils.isEmpty(password) || password.length() < 8) {
            return BA.str(R.string.password_min8);
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isLetter(c)) hasLetter = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }
        if (!hasLetter) {
            return BA.str(R.string.password_one_letter);
        }
        if (!hasDigit) {
            return BA.str(R.string.password_one_digit);
        }
        if (!password.equals(passwordConfirm)) {
            return BA.str(R.string.passwords_dont_match);
        }
        return null;
    }
}
