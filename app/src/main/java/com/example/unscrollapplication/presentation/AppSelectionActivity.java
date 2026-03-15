package com.example.unscrollapplication.presentation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unscrollapplication.R;
import com.example.unscrollapplication.domain.AppInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppSelectionActivity extends AppCompatActivity {

    private static final String PREF_NAME = "scroll_prefs";
    private static final String KEY_SELECTED_APPS = "selected_apps";

    private RecyclerView rvApps;
    private Button btnSaveSelection;
    private AppAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);

        rvApps = findViewById(R.id.rvApps);
        btnSaveSelection = findViewById(R.id.btnSaveSelection);

        List<AppInfo> apps = loadInstalledUserApps();
        Log.d("AppSelection", "Apps loaded: " + apps.size());  // ADD THIS

        markPreviouslySelected(apps);
        Log.d("AppSelection", "After marking selected: " + apps.size());  // ADD THIS

        adapter = new AppAdapter(apps);
        rvApps.setLayoutManager(new LinearLayoutManager(this));
        rvApps.setAdapter(adapter);

        btnSaveSelection.setOnClickListener(v -> {
            saveSelection(adapter.getAppList());
            finish();
        });
    }

    private List<AppInfo> loadInstalledUserApps() {
        List<AppInfo> result = new ArrayList<>();

        PackageManager pm = getPackageManager();
        String myPackage = getPackageName();

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);

        for (ResolveInfo resolveInfo : resolveInfos) {

            String packageName = resolveInfo.activityInfo.packageName;

            // Exclude own app
            if (myPackage.equals(packageName)) {
                continue;
            }

            String label = resolveInfo.loadLabel(pm).toString();
            ApplicationInfo appInfo = resolveInfo.activityInfo.applicationInfo;
            boolean relevant = isScrollRelevantApp(packageName, label);
            if (!relevant && isSystemApp(appInfo)) {
                continue;
            }
            if (!relevant) {
                continue;
            }

            result.add(new AppInfo(label, packageName, false));
        }

        return result;
    }

    private boolean isSystemApp(ApplicationInfo appInfo) {
        int flags = appInfo.flags;
        return (flags & ApplicationInfo.FLAG_SYSTEM) != 0
                || (flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
    }

    private boolean isScrollRelevantApp(String packageName, String label) {
        String pkg = packageName.toLowerCase();
        String name = label.toLowerCase();

        Set<String> allowlist = new HashSet<>();
        // Social / short-form
        allowlist.add("com.facebook.katana");
        allowlist.add("com.facebook.lite");
        allowlist.add("com.instagram.android");
        allowlist.add("com.zhiliaoapp.musically"); // TikTok
        allowlist.add("com.ss.android.ugc.trill"); // TikTok Lite
        allowlist.add("com.snapchat.android");
        allowlist.add("com.twitter.android");
        allowlist.add("com.reddit.frontpage");
        allowlist.add("com.pinterest");
        allowlist.add("com.linkedin.android");
        allowlist.add("com.google.android.youtube");
        allowlist.add("com.google.android.apps.youtube.music");

        // Browsers
        allowlist.add("com.android.chrome");
        allowlist.add("com.chrome.beta");
        allowlist.add("com.chrome.dev");
        allowlist.add("com.chrome.canary");
        allowlist.add("org.mozilla.firefox");
        allowlist.add("org.mozilla.firefox_beta");
        allowlist.add("org.mozilla.fenix"); // Firefox
        allowlist.add("com.microsoft.emmx"); // Edge
        allowlist.add("com.opera.browser");
        allowlist.add("com.opera.browser.beta");
        allowlist.add("com.opera.mini.native");
        allowlist.add("com.brave.browser");
        allowlist.add("com.vivaldi.browser");

        if (allowlist.contains(packageName)) {
            return true;
        }

        // Heuristic fallback for similarly-named apps
        if (pkg.contains("facebook") || name.contains("facebook")) return true;
        if (pkg.contains("instagram") || name.contains("instagram")) return true;
        if (pkg.contains("tiktok") || name.contains("tiktok")) return true;
        if (pkg.contains("snapchat") || name.contains("snapchat")) return true;
        if (pkg.contains("twitter") || name.contains("twitter") || name.contains("x")) return true;
        if (pkg.contains("reddit") || name.contains("reddit")) return true;
        if (pkg.contains("pinterest") || name.contains("pinterest")) return true;
        if (pkg.contains("linkedin") || name.contains("linkedin")) return true;
        if (pkg.contains("youtube") || name.contains("youtube")) return true;
        if (pkg.contains("browser") || name.contains("browser")) return true;
        if (pkg.contains("chrome") || name.contains("chrome")) return true;
        if (pkg.contains("firefox") || name.contains("firefox")) return true;
        if (pkg.contains("edge") || name.contains("edge")) return true;
        if (pkg.contains("opera") || name.contains("opera")) return true;
        if (pkg.contains("brave") || name.contains("brave")) return true;
        if (pkg.contains("vivaldi") || name.contains("vivaldi")) return true;

        return false;
    }


    private void markPreviouslySelected(List<AppInfo> apps) {
        Set<String> selectedPackages = getSelectedPackages(this);
        if (selectedPackages == null || selectedPackages.isEmpty()) {
            return;
        }

        for (AppInfo app : apps) {
            if (selectedPackages.contains(app.packageName)) {
                app.selected = true;
            }
        }
    }

    private void saveSelection(List<AppInfo> apps) {
        Set<String> selected = new HashSet<>();
        for (AppInfo app : apps) {
            if (app.selected) {
                selected.add(app.packageName);
            }
        }

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        prefs.edit().putStringSet(KEY_SELECTED_APPS, selected).apply();
    }

    public static Set<String> getSelectedPackages(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getStringSet(KEY_SELECTED_APPS, new HashSet<>());
    }
}
