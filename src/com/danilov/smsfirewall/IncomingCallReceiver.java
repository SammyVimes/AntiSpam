package com.danilov.smsfirewall;

import java.lang.reflect.Method;
import java.util.ArrayList;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class IncomingCallReceiver extends BroadcastReceiver {
	
	ArrayList<String> list = new ArrayList<String>();
	Context context;

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sPref = context.getSharedPreferences("preferences", Context.MODE_WORLD_READABLE);
	    String savedText = sPref.getString(SettingsActivity.BLOCK_PARAMETER, "");
	    if(savedText.equals(SettingsActivity.NOT_CHECKED)){
	    	return;
	    }
		Bundle extras = intent.getExtras();
		this.context = context;
		boolean isSpam = false;
		String phoneNumber = new String();
	    if (extras != null) {
	    	String state = extras.getString(TelephonyManager.EXTRA_STATE);
	    	if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
	    		phoneNumber = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
	    		isSpam = checkNumber(phoneNumber);
	    	}
	    }
	    if(isSpam){
	    	toaster(context.getResources().getString(R.string.callRejected) + " " + phoneNumber);
	    	TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
	    	endCall(telephonyManager);
	    }
	}
	
	private void endCall(TelephonyManager telephonyManager){
	    	try{
	    		final Class c = Class.forName(telephonyManager.getClass().getName());
	    		final Method m = c.getDeclaredMethod("getITelephony");
	    		m.setAccessible(true);
	    		
	    		com.android.internal.telephony.ITelephony telephonyService = (com.android.internal.telephony.ITelephony)m.invoke(telephonyManager);
	    		
	    		Log.d("MY PHONE STATE LISTENER", "ENDING CALL");
	    		telephonyService.endCall();
	    		telephonyService.silenceRinger();
	    	}
	    	catch (Exception e){
	            e.printStackTrace();
	            Log.e("CALL",
	                    "FATAL ERROR: could not connect to telephony subsystem");
	            Log.e("CALL", "Exception object: " + e);
	    	}
	    	
	    	Log.d("MY PHONE STATE LISTENER", "CALL ENDED SUCCESSFULLY");
	}
	
	
	private boolean checkNumber(String number){
		boolean isSpam = false;
		DBHelper dbHelper = new DBHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor c = db.query("mytable", null, null, null, null, null, null);
		if (c.moveToFirst()) {
			int nameColIndex = c.getColumnIndex("number");
			do {
		        list.add(c.getString(nameColIndex));
		    } while (c.moveToNext());
		}
		for(int i = 0; i < list.size(); i++){
			if(list.get(i).equals(number)){
				isSpam = true;
				break;
			}
		}
		c.close();
		return isSpam;
	}
	
	public void toaster(String text){
		Toast t = Toast.makeText(context, text, Toast.LENGTH_LONG);
		t.show();
	}

}
