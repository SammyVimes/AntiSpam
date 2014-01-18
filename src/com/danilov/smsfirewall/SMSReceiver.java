package com.danilov.smsfirewall;

import java.util.ArrayList;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("UseValueOf")
public class SMSReceiver extends BroadcastReceiver {
	
	public static final String SENDER = "SENDER";
	public static final String MESSAGE = "MESSAGE";
	public static final String DATE = "DATE";
	
	private Resources resources;
	
	ArrayList<String> list = new ArrayList<String>();
	Context context;
	
	@Override
	public void onReceive(Context context, Intent arg1) {		
		SharedPreferences sPref = context.getSharedPreferences("preferences", Context.MODE_WORLD_READABLE);
		boolean blockUnknowChecked = sPref.getBoolean(SettingsActivity.BLOCK_UNKNOWN_PARAMETER, false);
		boolean showNotificationsChecked = sPref.getBoolean(SettingsActivity.SHOW_NOTIFICATION_PARAMETER, false);
		resources = context.getResources();
		this.context = context;
		DBHelperWhitelist dbHelperWhitelist = new DBHelperWhitelist(context);
		String sender = new String();
		SmsMessage msgs[] = getMessagesFromIntent(arg1);
		sender = msgs[0].getDisplayOriginatingAddress();
		if (dbHelperWhitelist.contains(sender)) {
			return;
		}
		if (blockUnknowChecked) {
			if (!Util.isInContacts(context, sender)) {
				abortBroadcast();
				Sms sms = getSms(msgs);
				DBSpamCacheHelper helper = new DBSpamCacheHelper(context);
				helper.add(sms.getAddress(), sms.getText(), sms.getDate());
				helper.close();
				Toast toast = Toast.makeText(context, resources.getString(R.string.blockedMessageUnknown), Toast.LENGTH_SHORT);
				toast.show();
				return;
			}
		}
		DBHelper dbHelper = new DBHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor c = db.query("mytable", null, null, null, null, null, null);
		if (c.moveToFirst()) {
			int nameColIndex = c.getColumnIndex("number");
			do {
		        list.add(c.getString(nameColIndex));
		    } while (c.moveToNext());
		}
		db.close();
		c.close();
		boolean needToCheck = true;
		Sms sms = getSms(msgs);
		for(int j = 0; j < list.size(); j++){
			if(list.get(j).toLowerCase(Locale.getDefault()).contains(sms.getAddress().toLowerCase(Locale.getDefault()))){
				if (showNotificationsChecked) {
					NotificationHelper notificationHelper = NotificationHelper.getNotificationHelper(context);
					notificationHelper.updateNotification();
				}
				DBSpamCacheHelper helper = new DBSpamCacheHelper(context);
				helper.add(sms.getAddress(), sms.getText(), sms.getDate());
				helper.close();
				Toast toast = Toast.makeText(context, resources.getString(R.string.blockedMessage) + " " + sender, Toast.LENGTH_SHORT);
				toast.show();
				abortBroadcast();
				return;
			}
		}
		if(needToCheck){
			if(isSpam(sms.getText())){
				abortBroadcast();
				Intent intent = new Intent(context, AddToSpamActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra(MESSAGE, sms.getText());
				intent.putExtra(SENDER, sms.getAddress());
				intent.putExtra(DATE, Long.valueOf(sms.getDate()).toString());
				context.startActivity(intent);
			}
		}
	}
	
	private Sms getSms(final SmsMessage[] parts) {
		Sms sms = new Sms();
		String message = "";
		sms.setAddress(parts[0].getDisplayOriginatingAddress());
		sms.setDate(parts[0].getTimestampMillis());
		for (SmsMessage msg : parts) {
			message = message + msg.getDisplayMessageBody();
		}
		sms.setText(message);
		return sms;
	}
	
	private SmsMessage[] getMessagesFromIntent(Intent intent) {
		SmsMessage retMsgs[] = null;
		Bundle bdl = intent.getExtras();
		try {
			Object pdus[] = (Object[]) bdl.get("pdus");
			retMsgs = new SmsMessage[pdus.length];
			for (int n = 0; n < pdus.length; n++) {
				byte[] byteData = (byte[]) pdus[n];
				retMsgs[n] = SmsMessage.createFromPdu(byteData);
			}

		} catch (Exception e) {
			Log.e("SMS FireWall", "GetMessages ERROR\n" + e);
		}
		return retMsgs;
	}
	
	public boolean isSpam(String message){
		String[] suspiciousWords = resources.getStringArray(R.array.suspicious_words_array);
		DBHelperSuspicious dbHelper = new DBHelperSuspicious(context);
		ArrayList<String> list = dbHelper.getList();
		boolean spam = false;
		for(int i = 0; i < suspiciousWords.length; i++){
			if(message.toLowerCase(Locale.getDefault()).contains(suspiciousWords[i])){
				spam = true;
				break;
			}
		}
		if(!spam){
			for(int i = 0; i < list.size(); i++){
				if(message.toLowerCase(Locale.getDefault()).contains(list.get(i))){
					spam = true;
					break;
				}
			}
		}
		return spam;
	}


}
