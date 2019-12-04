package com.click.clicklib;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Utils {
//	/** key 값에 해당한 value 를 저장한다 **/
	public static void setSOString(Context context, String key, String value) throws NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
		SharedPreferences shrdPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = shrdPref.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
//	/** key 값에 해당한 value를 리턴한다.**/
	public static String getSOString(Context context, String key){
		SharedPreferences shrdPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		String ret = shrdPref.getString(key, "");
		return ret;
	}

	public static String GetDecodeHTML(String html) {
		String ret = html;
		try {
			ret = URLDecoder.decode(html, "UTF-8");
		} catch (Exception e) {
		}
		return ret;
	}

	public static boolean isServiceRunning(Context context) {
		ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		
		for (RunningServiceInfo rsi : am.getRunningServices(Integer.MAX_VALUE)) {
			if (MainService.class.getName().equals(rsi.service.getClassName()))
				return true;
		}
		
		return false;
	}
}
