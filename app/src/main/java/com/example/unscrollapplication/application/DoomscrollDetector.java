package com.example.unscrollapplication.application;

import android.content.Context;

import com.example.unscrollapplication.data.DoomscrollStore;

public class DoomscrollDetector {

    private static final long GAP_RESET_MS = 20_000; // if user pauses longer, reset window

    public static void onScroll(Context context) {
        if (!DoomscrollStore.isEnabled(context)) {
            return;
        }
        if (DoomscrollStore.isAlarmActive(context)) {
            return;
        }

        long now = System.currentTimeMillis();
        long first = DoomscrollStore.getFirstScrollMs(context);
        long last = DoomscrollStore.getLastScrollMs(context);

        if (first == 0 || last == 0) {
            DoomscrollStore.resetWindow(context, now);
            return;
        }

        if (now - last > GAP_RESET_MS) {
            DoomscrollStore.resetWindow(context, now);
            return;
        }

        DoomscrollStore.setLastScrollMs(context, now);

        long windowMs = DoomscrollStore.getMinutes(context) * 60_000L;
        if (now - first >= windowMs) {
            DoomscrollAlarm.trigger(context);
        }
    }
}
