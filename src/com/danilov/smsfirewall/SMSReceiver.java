package com.danilov.smsfirewall;

import java.util.ArrayList;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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

	@Override
	public void onReceive(Context context, Intent arg1) {
		resources = context.getResources();
		DBHelper dbHelper = new DBHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor c = db.query("mytable", null, null, null, null, null, null);
		if (c.moveToFirst()) {
			int nameColIndex = c.getColumnIndex("name");
			do {
		        list.add(c.getString(nameColIndex));
		    } while (c.moveToNext());
		}
		c.close();
		String sender = new String();
		String message = new String();
		String date = new String();
		SmsMessage msgs[] = getMessagesFromIntent(arg1);
		boolean needToCheck = true;
		for (int i = 0; i < msgs.length; i++) {
			SmsMessage mesg = msgs[i];
			date = new Long(mesg.getTimestampMillis()).toString();
			sender = mesg.getDisplayOriginatingAddress();
			message = message + mesg.getDisplayMessageBody();
			for(int j = 0; j < list.size(); j++){
				if(list.get(j).toLowerCase(Locale.getDefault()).contains(sender.toLowerCase(Locale.getDefault()))){
					abortBroadcast();
					Toast toast = Toast.makeText(context, resources.getString(R.string.blockedMessage) + " " + sender, Toast.LENGTH_SHORT);
					toast.show();
					needToCheck = false;
					break;
				}
			}
		}
		if(needToCheck){
			if(isSpam(message)){
				abortBroadcast();
				Intent intent = new Intent(context, AddToSpamActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra(MESSAGE, message);
				intent.putExtra(SENDER, sender);
				intent.putExtra(DATE, date);
				context.startActivity(intent);
			}
		}
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
		boolean spam = false;
		for(int i = 0; i < suspiciousWords.length; i++){
			if(message.toLowerCase(Locale.getDefault()).contains(suspiciousWords[i])){
				spam = true;
				break;
			}
		}
		return spam;
	}


}
