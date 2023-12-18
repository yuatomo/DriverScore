package com.example.driverscore;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "DriverScore";
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("onReceive");
        // 通知を表示
        int notificationId = intent.getIntExtra("notificationId", 1);
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        int color = 0;
        if(intent.getStringExtra("color").equals("赤")) {
            color = R.color.red;
        } else if(intent.getStringExtra("color").equals("黄")) {
            color = R.color.yellow;
        } else if(intent.getStringExtra("color").equals("青")) {
            color = R.color.blue;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        showNotification(context, pendingIntent, notificationId, title, content, color);
    }

    private void showNotification(Context context, PendingIntent pendingIntent, int notificationId, String title, String content, int color) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setColor(ContextCompat.getColor(context, color));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}