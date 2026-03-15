// MainActivity.java
package com.example.unscrollapplication.presentation;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import com.example.unscrollapplication.R;
import com.example.unscrollapplication.application.DoomscrollAlarm;
import com.example.unscrollapplication.application.ScrollTrackingService;
import com.example.unscrollapplication.data.DoomscrollStore;
import com.example.unscrollapplication.data.ScrollScoreStore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvServiceStatus;
    private TextView tvScrollScore;
    private Button btnSelectApps;
    private Button btnStopAlarm;
    private Button btnAlarmSettings;
    private Button btnFriends;
    private Button btnViewAll;
    private Button btnLogout;
    private RecyclerView rvTopRanks;
    private TopRanksAdapter topRanksAdapter;
    private TextView tvTopEmpty;
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notificationPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (!isGranted) {
                        Toast.makeText(this, "Enable notifications to hear alarms", Toast.LENGTH_LONG).show();
                    }
                });

        tvServiceStatus = findViewById(R.id.tvServiceStatus);
        tvScrollScore = findViewById(R.id.tvScrollScore);
        btnSelectApps = findViewById(R.id.btnSelectApps);
        btnStopAlarm = findViewById(R.id.btnStopAlarm);
        btnAlarmSettings = findViewById(R.id.btnAlarmSettings);
        btnFriends = findViewById(R.id.btnFriends);
        btnViewAll = findViewById(R.id.btnViewAll);
        btnLogout = findViewById(R.id.btnLogout);
        rvTopRanks = findViewById(R.id.rvTopRanks);
        tvTopEmpty = findViewById(R.id.tvTopEmpty);

        topRanksAdapter = new TopRanksAdapter();
        rvTopRanks.setLayoutManager(new LinearLayoutManager(this));
        rvTopRanks.setAdapter(topRanksAdapter);

        tvServiceStatus.setOnClickListener(v -> openAccessibilitySettings());

        btnSelectApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AppSelectionActivity.class);
                startActivity(intent);
            }
        });

        btnAlarmSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AlarmSettingsActivity.class);
            startActivity(intent);
        });

        btnFriends.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FriendsActivity.class);
            startActivity(intent);
        });

        btnViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LeaderboardActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnStopAlarm.setOnClickListener(v -> {
            DoomscrollAlarm.stop(this);
            updateUi();
        });

        requestNotificationPermissionIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUi();
    }

    private void updateUi() {
        boolean enabled = isAccessibilityServiceEnabled(this,
                ScrollTrackingService.class);

        if (enabled) {
            tvServiceStatus.setText("Service on");
        } else {
            tvServiceStatus.setText("Service off • enable to track");
        }

        long score = ScrollScoreStore.getTotalScrollCount(this);
        tvScrollScore.setText(String.valueOf(score));

        boolean alarmActive = DoomscrollStore.isAlarmActive(this);
        btnStopAlarm.setVisibility(alarmActive ? View.VISIBLE : View.GONE);

        ensureUsername();
        loadTopRanks();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    private void ensureUsername() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String username = doc.getString("username");
                    if (TextUtils.isEmpty(username)) {
                        Intent intent = new Intent(MainActivity.this, UsernameSetupActivity.class);
                        startActivity(intent);
                    }
                });
    }

    private void loadTopRanks() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        String today = LocalDate.now().toString();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

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
                    fetchTopRanks(db, friendUids, today, user.getUid());
                })
                .addOnFailureListener(e -> tvTopEmpty.setVisibility(View.VISIBLE));
    }

    private void fetchTopRanks(FirebaseFirestore db, List<String> uids, String today, String selfUid) {
        List<TopRanksAdapter.TopRankItem> items = new ArrayList<>();
        if (uids.isEmpty()) {
            showTopRanks(items, selfUid);
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
                        items.add(new TopRanksAdapter.TopRankItem(uid, username, finalScore));
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            showTopRanks(items, selfUid);
                        }
                    })
                    .addOnFailureListener(e -> {
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            showTopRanks(items, selfUid);
                        }
                    });
        }
    }

    private void showTopRanks(List<TopRanksAdapter.TopRankItem> items, String selfUid) {
        items.sort(Comparator.comparingLong(a -> a.dailyScore));
        if (items.size() > 5) {
            items = items.subList(0, 5);
        }
        topRanksAdapter.setItems(items, selfUid);
        tvTopEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openAccessibilitySettings() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    // One common way: inspect ENABLED_ACCESSIBILITY_SERVICES string.[web:25][web:22]
    public static boolean isAccessibilityServiceEnabled(Context context,
                                                        Class<?> serviceClass) {
        String prefString = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

        if (prefString == null) {
            return false;
        }

        String serviceId = new ComponentName(context, serviceClass).flattenToString();
        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
        splitter.setString(prefString);

        while (splitter.hasNext()) {
            String componentName = splitter.next();
            if (componentName.equalsIgnoreCase(serviceId)) {
                return true;
            }
        }

        return false;
    }
}
