package com.example.schoolthing.data.local;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages local user registration, credential persistence, and session state.
 */
public class UserManager {

    private static final String PREF_NAME = "qr_user_prefs";
    private static final String KEY_IS_REGISTERED = "is_registered";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_ID_NUMBER = "id_number";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_ROLE = "role";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void registerUser(Context context, String name, String idNumber, String email, String password, String role) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(KEY_IS_REGISTERED, true);
        editor.putString(KEY_FULL_NAME, name);
        editor.putString(KEY_ID_NUMBER, idNumber);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_ROLE, role);
        editor.apply();
    }

    public static boolean isUserRegistered(Context context) {
        return getPrefs(context).getBoolean(KEY_IS_REGISTERED, false);
    }

    public static boolean validateLogin(Context context, String inputEmailOrId, String inputPassword) {
        if (!isUserRegistered(context)) {
            return false;
        }
        SharedPreferences prefs = getPrefs(context);
        String savedEmail = prefs.getString(KEY_EMAIL, "");
        String savedId = prefs.getString(KEY_ID_NUMBER, "");
        String savedPassword = prefs.getString(KEY_PASSWORD, "");

        boolean matchesUser = inputEmailOrId.equalsIgnoreCase(savedEmail) || inputEmailOrId.equalsIgnoreCase(savedId);
        boolean matchesPassword = inputPassword.equals(savedPassword);

        return matchesUser && matchesPassword;
    }

    public static String getSavedName(Context context) {
        return getPrefs(context).getString(KEY_FULL_NAME, "Registered User");
    }

    public static String getSavedId(Context context) {
        return getPrefs(context).getString(KEY_ID_NUMBER, "STU-0000");
    }

    public static String getSavedEmail(Context context) {
        return getPrefs(context).getString(KEY_EMAIL, "");
    }

    public static String getSavedRole(Context context) {
        return getPrefs(context).getString(KEY_ROLE, "Student Pass");
    }

    public static void clearUser(Context context) {
        getPrefs(context).edit().clear().apply();
    }
}
