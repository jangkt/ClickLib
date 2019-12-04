package com.click.clicklib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

///** 폰이 부팅하면서 수행되는 클래스 **/
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
//			/** 폰이 부팅되면 서비스를 시작한다. **/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent in = new Intent(context, ReClickService.class);
                in.setAction(ReClickService.BACK);
                context.startForegroundService(in);
                Log.i("test1", "실행2");
            } else {
                Intent in = new Intent(context, MainService.class);
                context.startService(in);
            }
        }
    }
}
