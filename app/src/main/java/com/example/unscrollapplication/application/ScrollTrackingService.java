package com.example.unscrollapplication.application;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.example.unscrollapplication.data.ScrollScoreStore;
import com.example.unscrollapplication.presentation.AppSelectionActivity;

import java.util.Set;

public class ScrollTrackingService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) {
            return;
        }
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED  ) {

         Log.d("ACC_EVENT", "Type: " + event.getEventType() +
                " Package: " + event.getPackageName());



            CharSequence pkgCs = event.getPackageName();
            if (pkgCs == null) {
                return;
            }

            String pkg = pkgCs.toString();

            Set<String> selectedPackages = AppSelectionActivity.getSelectedPackages(getApplicationContext());
            if (!selectedPackages.contains(pkg)) {
                return; // not a tracked app
            }

            ScrollScoreStore.registerScroll(getApplicationContext());
            DoomscrollDetector.onScroll(getApplicationContext());
        }
    }

    @Override
    public void onInterrupt() {
    }
}
