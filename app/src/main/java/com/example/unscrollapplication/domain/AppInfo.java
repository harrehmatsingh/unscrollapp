package com.example.unscrollapplication.domain;

public class AppInfo {
    public String appName;
    public String packageName;
    public boolean selected;

    public AppInfo(String appName, String packageName, boolean selected) {
        this.appName = appName;
        this.packageName = packageName;
        this.selected = selected;
    }
}
