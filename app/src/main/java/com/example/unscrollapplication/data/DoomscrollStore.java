package com.example.unscrollapplication.data;

import android.content.Context;
import android.content.SharedPreferences;

public class DoomscrollStore {

    private static final String PREF_NAME = "scroll_prefs";
    private static final String KEY_DOOM_ENABLED = "doom_enabled";
    private static final String KEY_DOOM_MINUTES = "doom_minutes";
    private static final String KEY_ALARM_ACTIVE = "doom_alarm_active";
    private static final String KEY_FIRST_SCROLL_MS = "doom_first_scroll_ms";
    private static final String KEY_LAST_SCROLL_MS = "doom_last_scroll_ms";

    public static boolean isEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DOOM_ENABLED, false);
    }

    public static void setEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DOOM_ENABLED, enabled).apply();
    }

    public static int getMinutes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_DOOM_MINUTES, 10);
    }

    public static void setMinutes(Context context, int minutes) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_DOOM_MINUTES, minutes).apply();
    }

    public static boolean isAlarmActive(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ALARM_ACTIVE, false);
    }

    public static void setAlarmActive(Context context, boolean active) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ALARM_ACTIVE, active).apply();
    }

    public static long getFirstScrollMs(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_FIRST_SCROLL_MS, 0);
    }

    public static void setFirstScrollMs(Context context, long value) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_FIRST_SCROLL_MS, value).apply();
    }

    public static long getLastScrollMs(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_LAST_SCROLL_MS, 0);
    }

    public static void setLastScrollMs(Context context, long value) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_LAST_SCROLL_MS, value).apply();
    }

    public static void resetWindow(Context context, long nowMs) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putLong(KEY_FIRST_SCROLL_MS, nowMs)
                .putLong(KEY_LAST_SCROLL_MS, nowMs)
                .apply();
    }
}
