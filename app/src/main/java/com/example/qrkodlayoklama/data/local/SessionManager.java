package com.example.qrkodlayoklama.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public final class SessionManager {
    private static SharedPreferences prefs;
    private static String tokenCache;

    private SessionManager() {}

    public static void init(Context ctx) {
        if (prefs == null) {
            prefs = ctx.getSharedPreferences("session_prefs", Context.MODE_PRIVATE);
            tokenCache = prefs.getString("access_token", null);
        }
    }

    public static void setToken(String token) {
        tokenCache = token;
        if (prefs != null) {
            prefs.edit().putString("access_token", token).apply();
        }
    }

    public static String getToken() {
        if (tokenCache == null && prefs != null) {
            tokenCache = prefs.getString("access_token", null);
        }
        return tokenCache;
    }

    public static boolean isLoggedIn() {
        String t = getToken();
        return t != null && !t.isEmpty();
    }

    public static void clear() {
        tokenCache = null;
        if (prefs != null) {
            prefs.edit().remove("access_token").apply();
        }
    }
}
