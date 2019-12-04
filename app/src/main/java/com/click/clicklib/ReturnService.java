package com.click.clicklib;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class ReturnService extends Service {
    public static int COUNT;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("test1", "실행둘째포그");
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default2");
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentTitle(null);
        NotificationChannel nc = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nc = new NotificationChannel("default2", "기본 채널2", NotificationManager.IMPORTANCE_NONE);
        }
        builder.setContentText(null);
        Intent notificationIntent = new Intent(this, Act1.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);

        final NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(nc);
        }
        startForeground(startId, builder.build());

        int count = intent.getExtras().getInt("COUNT");
        if (count == 20) {
            Intent intent1 = new Intent(this, MainService.class);
            startService(intent1);
            COUNT = 0;
            stopSelf();
        }else {
            Log.i("test1", "서비스 대기중..."+count);
            COUNT = count;
            stopSelf();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Intent alarmIntent = new Intent(ReturnService.this, ReClickService.class);
        alarmIntent.setAction(ReClickService.FORE);
        Log.i("test1", "두번째" + COUNT);
        alarmIntent.putExtra("COUNT", COUNT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(alarmIntent);
        }
        Log.i("test1", "두번째");
        super.onDestroy();
    }
}
