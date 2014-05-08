package org.nypr.cordova.wakeupplugin;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class WakeupReceiver extends BroadcastReceiver {

	private static final String LOG_TAG = "WakeupReceiver";

	@SuppressLint({ "SimpleDateFormat", "NewApi" })
	@Override
	public void onReceive(Context context, Intent intent) {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Log.d(LOG_TAG, "wakeuptimer expired at " + sdf.format(new Date().getTime()));
	
		try {
			String packageName = context.getPackageName();
			Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
			String className = launchIntent.getComponent().getClassName();		    	
			Log.d(LOG_TAG, "launching activity for class " + className);

			@SuppressWarnings("rawtypes")
			Class c = Class.forName(className); 

			Intent i = new Intent(context, c);
			i.putExtra("wakeup", true);
			i.putExtra("extra", intent.getExtras().get("extra").toString());
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);

			// repeat in one week
			Date next = new Date(new Date().getTime() + (7 * 24 * 60 * 60 * 1000));
			Log.d(LOG_TAG,"resetting alarm at " + sdf.format(next));

			Intent reschedule = new Intent(context, WakeupReceiver.class);
			reschedule.putExtra("extra", intent.getExtras().get("extra").toString());
			reschedule.putExtra("day", WakeupPlugin.daysOfWeek.get(intent.getExtras().get("day")));

			PendingIntent sender = PendingIntent.getBroadcast(context, 19999 + WakeupPlugin.daysOfWeek.get(intent.getExtras().get("day")), intent, PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			if (Build.VERSION.SDK_INT>=19) {
				alarmManager.setExact(AlarmManager.RTC_WAKEUP, next.getTime(), sender);
			} else {
				alarmManager.set(AlarmManager.RTC_WAKEUP, next.getTime(), sender);
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}