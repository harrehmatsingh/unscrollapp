// ScrollScoreStore.java
package com.example.unscrollapplication.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.LocalDate;

public class ScrollScoreStore {

    private static final String PREF_NAME = "scroll_prefs";
    private static final String KEY_TOTAL_SCROLLS = "total_scrolls";
    private static final String KEY_LAST_RESET_DATE = "last_reset_date";

    private static long lastScrollTime = 0;
    private static final long SCROLL_THRESHOLD_MS = 500; // adjust

    private static void incrementScrollCount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long current = prefs.getLong(KEY_TOTAL_SCROLLS, 0);
        long updated = current + 1;
        prefs.edit().putLong(KEY_TOTAL_SCROLLS, updated).apply();
        FirestoreSync.syncDailyScroll(context, updated);
    }

    public static long getTotalScrollCount(Context context) {
        ensureDailyReset(context);
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_TOTAL_SCROLLS, 0);
    }



    public static void registerScroll(Context context) {
        ensureDailyReset(context);
        long now = System.currentTimeMillis();

        if (now - lastScrollTime > SCROLL_THRESHOLD_MS) {
            lastScrollTime = now;
            incrementScrollCount(context);
        }
    }

    private static void ensureDailyReset(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String today = LocalDate.now().toString(); // device-local date, yyyy-MM-dd
        String lastReset = prefs.getString(KEY_LAST_RESET_DATE, null);

        if (!today.equals(lastReset)) {
            prefs.edit()
                    .putLong(KEY_TOTAL_SCROLLS, 0)
                    .putString(KEY_LAST_RESET_DATE, today)
                    .apply();
            lastScrollTime = 0;
            FirestoreSync.syncDailyScroll(context, 0);
        }
    }
}
