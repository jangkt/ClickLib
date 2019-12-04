package com.click.clicklib;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.Timer;
import java.util.TimerTask;

///** 폰이 재부팅되면서 수행되는 클래스 **/
public class RestartReceiver extends BroadcastReceiver {
    public static String ACTION_RESTART_CLICK_SERVICE = "ACTION_RESTART_CLICK_SERVICE";
    public static String ACTION_RETURN_CLICK_SERVICE = "ACTION_RETURN_CLICK_SERVICE";
    static Context context;

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(final Context context, final Intent intent) {
        this.context = context;
        Log.i("test1", "실행3.0  " + context.toString());
        if (intent.getAction().equals(ACTION_RESTART_CLICK_SERVICE)) {
            this.clearAbortBroadcast();
            Log.i("test1", "실행3.1");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Timer timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        if (!isLaunchingService(context)) {
                            Intent in = new Intent(context, ReClickService.class);
                            in.setAction(ReClickService.FIRST);
                            in.putExtra("COUNT",0);
                            context.startForegroundService(in);
                        }
                    }
                };

                timer.schedule(task, 2000);
            } else {
                Timer timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        if (!isLaunchingService(context)) {
                            Log.i("test1", "스타트2");
                            Intent in = new Intent(context, MainService.class);
                            context.startService(in);
                        }
                    }
                };
                timer.schedule(task, 2000);
            }
        }
    }

    public Boolean isLaunchingService(Context mContext) {

        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ReClickService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }
}
