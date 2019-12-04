package com.click.clicklib;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ReClickService extends Service {
    public static String FORE = "FORE";
    public static String FIRST = "FIRST";
    public static String BACK = "BACK";
    public static TimerTask task;
    public static int COUNT;

//    PowerConnectionReceiver receiver = new PowerConnectionReceiver();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.i("test1", "실행");
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentTitle(null);
        NotificationChannel nc = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nc = new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_NONE);
        }

        builder.setContentText(null);
        Intent notificationIntent = new Intent(this, Act1.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);

        final NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(nc);
        }
        startForeground(startId, builder.build());
//        final IntentFilter filter = new IntentFilter();
//        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
//        filter.addAction(Intent.ACTION_POWER_CONNECTED);
//        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
//        registerReceiver(receiver, filter);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

//                if (!receiver.plugOn.equals("USB")) {

                    if (intent.getAction().equals(FORE)) {

                        Timer timer = new Timer();
                        final int[] count = {0, 0};
                        count[1] = intent.getExtras().getInt("COUNT");
                        task = new TimerTask() {

                            @Override
                            public void run() {
                                if (count[0] >= 9) {
                                    count[1]++;
                                    count[0] = 0;
                                    COUNT = count[1];
                                    cancel();
                                    stopSelf();
                                }
                                Log.i("test1", "" + count[0]);
                                serviceList();
                                count[0]++;
                            }
                        };
                        timer.schedule(task, 0, 3000);
                    } else if (intent.getAction().equals(BACK)) {
                        Timer timer = new Timer();
                        final int[] count = {0, 0};
                        task = new TimerTask() {
                            @Override
                            public void run() {
                                if (count[0] >= 9) {
                                    count[0] = 0;
                                    cancel();
                                    stopSelf();
                                }
                                Log.i("test1", "" + count[0]);

                                count[0]++;
                            }
                        };
                        timer.schedule(task, 0, 3000);
                    } else if (intent.getAction().equals(FIRST)) {
                        Timer timer = new Timer();
                        final int[] count = {0, 0};
                        task = new TimerTask() {
                            @Override
                            public void run() {
                                if (count[0] >= 9) {
                                    count[1]++;
                                    count[0] = 0;
                                    COUNT = count[1];
                                    cancel();
                                    stopSelf();
                                }
                                Log.i("test1", "" + count[0]);
                                serviceList();
                                count[0]++;
                            }
                        };
                        timer.schedule(task, 0, 3000);
                    }

//                } else {
//                    Log.i("test0", "USB연결이다.");
////                    unregisterReceiver(receiver);
//                    Handler handler1 = new Handler();
//                    handler1.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//
//                            stopSelf();
//                        }
//                    },30000);
//
//                }
            }
        }, 500);


        stopForeground(true);
        return START_STICKY;
    }

    public void serviceList() {
        /*서비스 리스트*/
        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(1000);

        for (int i = 0; i < rs.size(); i++) {
            ActivityManager.RunningServiceInfo rsi = rs.get(i);
            Log.i("test1", "Package Name : " + rsi.service.getPackageName());
            Log.i("test1", "Class Name : " + rsi.service.getClassName());
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {

        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent alarmIntent = new Intent(ReClickService.this, ReturnService.class);
        Log.i("test1", "포그라운드종료" + COUNT);
        alarmIntent.putExtra("COUNT", COUNT);
        startForegroundService(alarmIntent);
        Log.i("test1", "포그라운드종료");

    }
}
