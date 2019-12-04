package com.click.clicklib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

public class PowerConnectionReceiver extends BroadcastReceiver {
    public String plugOn;
    int count;
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        count++;
        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            plugOn = onBatteryChanged(intent);
        }else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)){

        }
        Log.i("test0", String.valueOf(count));
    }
    public String onBatteryChanged(Intent intent) {
        int plug;
        String sPlug = "";


        if (intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false) == false) {
            return null;
        }
        plug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);

        switch (plug) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                sPlug = "AC";
                Log.i("test0", sPlug);
                return sPlug;

            case BatteryManager.BATTERY_PLUGGED_USB:
                sPlug = "USB";
                Log.i("test0", sPlug);
                return sPlug;
            default:
                sPlug = "BATTERY";
                Log.i("test0", sPlug);
                return sPlug;
        }
    }



}