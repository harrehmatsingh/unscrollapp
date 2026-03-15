package com.example.unscrollapplication.data;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class FirestoreSync {

    private static final long SYNC_THROTTLE_MS = 30_000;
    private static long lastSyncMs = 0;

    public static void syncDailyScroll(Context context, long dailyCount) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastSyncMs < SYNC_THROTTLE_MS) {
            return;
        }
        lastSyncMs = now;

        String today = LocalDate.now().toString();
        Map<String, Object> data = new HashMap<>();
        data.put("dailyScore", dailyCount);
        data.put("dailyScoreDate", today);
        data.put("updatedAt", now);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .set(data, com.google.firebase.firestore.SetOptions.merge());
    }
}
