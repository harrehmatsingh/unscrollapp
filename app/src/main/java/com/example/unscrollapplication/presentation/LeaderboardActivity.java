package com.example.unscrollapplication.presentation;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unscrollapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView rvLeaderboard;
    private ProgressBar pbLoading;
    private TextView tvEmpty;
    private LeaderboardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        rvLeaderboard = findViewById(R.id.rvLeaderboard);
        pbLoading = findViewById(R.id.pbLeaderboardLoading);
        tvEmpty = findViewById(R.id.tvLeaderboardEmpty);

        adapter = new LeaderboardAdapter();
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        rvLeaderboard.setAdapter(adapter);

        loadLeaderboard();
    }

    private void loadLeaderboard() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        pbLoading.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String today = LocalDate.now().toString();

        db.collection("users")
                .document(user.getUid())
                .collection("friends")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> friendUids = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String uid = doc.getString("uid");
                        if (uid != null) {
                            friendUids.add(uid);
                        }
                    }
                    friendUids.add(user.getUid());
                    fetchUsersForLeaderboard(db, friendUids, today, user.getUid());
                })
                .addOnFailureListener(e -> {
                    pbLoading.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                });
    }

    private void fetchUsersForLeaderboard(FirebaseFirestore db,
                                          List<String> uids,
                                          String today,
                                          String selfUid) {
        List<LeaderboardItem> items = new ArrayList<>();
        if (uids.isEmpty()) {
            showLeaderboard(items, selfUid);
            return;
        }

        final int[] remaining = {uids.size()};
        for (String uid : uids) {
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        String username = doc.getString("username");
                        String date = doc.getString("dailyScoreDate");
                        Long score = doc.getLong("dailyScore");
                        long finalScore = (date != null && date.equals(today) && score != null) ? score : 0;
                        if (username == null) {
                            username = "User";
                        }
                        items.add(new LeaderboardItem(uid, username, finalScore));
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            showLeaderboard(items, selfUid);
                        }
                    })
                    .addOnFailureListener(e -> {
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            showLeaderboard(items, selfUid);
                        }
                    });
        }
    }

    private void showLeaderboard(List<LeaderboardItem> items, String selfUid) {
        items.sort(Comparator.comparingLong(a -> a.dailyScore));
        adapter.setItems(items, selfUid);
        pbLoading.setVisibility(View.GONE);
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    static class LeaderboardItem {
        final String uid;
        final String username;
        final long dailyScore;

        LeaderboardItem(String uid, String username, long dailyScore) {
            this.uid = uid;
            this.username = username;
            this.dailyScore = dailyScore;
        }
    }
}
