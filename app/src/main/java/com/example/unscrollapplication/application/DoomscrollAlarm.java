package com.example.unscrollapplication.application;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.unscrollapplication.R;
import com.example.unscrollapplication.data.DoomscrollStore;
import com.example.unscrollapplication.presentation.MainActivity;

public class DoomscrollAlarm {

    private static final String CHANNEL_ID = "doomscroll_alarm_v2";
    private static final int NOTIFICATION_ID = 9001;
    public static final String ACTION_STOP_ALARM = "com.example.unscrollapplication.ACTION_STOP_ALARM";

    public static void trigger(Context context) {
        DoomscrollStore.setAlarmActive(context, true);
        ensureChannel(context);

        Intent stopIntent = new Intent(context, AlarmActionReceiver.class);
        stopIntent.setAction(ACTION_STOP_ALARM);
        PendingIntent stopPending = PendingIntent.getBroadcast(
                context,
                0,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent openIntent = new Intent(context, MainActivity.class);
        PendingIntent openPending = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Doomscroll alert")
                .setContentText("You’ve been scrolling for a while. Take a break.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setSound(sound)
                .setVibrate(new long[]{0, 400, 200, 400})
                .setContentIntent(openPending)
                .addAction(0, "Stop alarm", stopPending);

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build());
    }

    public static void stop(Context context) {
        DoomscrollStore.setAlarmActive(context, false);
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID);
    }

    private static void ensureChannel(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }
        NotificationChannel existing = manager.getNotificationChannel(CHANNEL_ID);
        if (existing != null) {
            return;
        }
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Doomscroll alarm",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Alerts when continuous scrolling exceeds your limit");
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{0, 500, 200, 500, 200, 700});
        channel.setSound(sound, attrs);
        manager.createNotificationChannel(channel);
    }
}
