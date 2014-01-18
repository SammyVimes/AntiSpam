package com.danilov.smsfirewall;

import java.util.Calendar;
import java.util.TimeZone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.NotificationCompat;

public class NotificationHelper {
	
	private static final int NOTIFICATION_ID = 25; //why not
	
	private Context context;
	private static NotificationHelper instance;
	
	private Notification notification;
	private NotificationManager notificationManager;
	
	private int dayInYear = 0;
	
	private int messagesCanceled = 0;
	
	private NotificationHelper(final Context context) {
		Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
		dayInYear = localCalendar.get(Calendar.DAY_OF_YEAR);
		this.context = context;
		notificationManager = (NotificationManager)context
                .getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	public static NotificationHelper getNotificationHelper(final Context context) {
		return instance == null ? (instance = new NotificationHelper(context)) : instance;
	}
	
	public void showNotification(final String message) {
		if (message == null) {
			return;
		}
        Bitmap bmp = ((BitmapDrawable)context.getResources().getDrawable(R.drawable.ic_launcher)).getBitmap();
		notification = new NotificationCompat.Builder(context)
						.setSmallIcon(R.drawable.ic_launcher)
						.setLargeIcon(bmp)
						.setContentIntent(getContentIntent())
						.setContentText(message)
						.getNotification();
		removeNotification();
		notificationManager.notify(NOTIFICATION_ID, notification);
	}
	
	private PendingIntent getContentIntent() {
		return PendingIntent.getActivity(context, 0, new Intent().setClass(context, SpamStoreActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0);
	}
	
	public void updateNotification() {
		Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
		int tmpDayInYear = localCalendar.get(Calendar.DAY_OF_YEAR);
		if (tmpDayInYear != dayInYear) {
			dayInYear = tmpDayInYear;
			clearData();
		}
		messagesCanceled++;
		String message = context.getResources().getString(R.string.notification_text);
		showNotification(message + messagesCanceled);
	}
	
	private void removeNotification() {
		notificationManager.cancel(NOTIFICATION_ID);
	}
	
	private void clearData() {
		messagesCanceled = 0;
		removeNotification();
	}
	
}
