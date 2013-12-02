package com.danilov.smsfirewall;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBSpamCacheHelper extends SQLiteOpenHelper {


	private static long TIME_TO_LIVE = 86400000;
	private static final long CLEAN_PERIOD = 43200000;
	
	private static final String DBVERSION = "0";
	private static final String VERSION = "VERSION_SPAM";
	private static final String LAST_CLEAN_PERFORMED_TIME = "CLEAN_TIME";
	
	private static final String ADDRESS_COLUMN = "address";
	private static final String MESSAGE_COLUMN = "message";
	private static final String DATE_COLUMN = "date";
	private static final String ID_COLUMN = "id";
	
	private static final String TABLE_NAME = "SPAM";
	
	
	
	private Context context;
	
	public DBSpamCacheHelper(Context context) {
		super(context, "spamdb", null, 1);
		SharedPreferences sPref = context.getSharedPreferences("preferences", context.MODE_WORLD_READABLE);
		int days = sPref.getInt(SettingsActivity.STORE_SPAM_DAYS, 1);
		long millis = days * 24 * 60 * 60 * 1000;
		TIME_TO_LIVE = millis;
		this.context = context;
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d("g", "--- onCreate Spam database ---");

	      db.execSQL("create table " + TABLE_NAME + " ("
	          + "id integer primary key autoincrement," 
	          + "address text, message text, date integer);");
	      SharedPreferences sPref = context.getSharedPreferences("preferences", context.MODE_WORLD_READABLE);
		  Editor ed = sPref.edit();
		  ed.putString(VERSION, DBVERSION);
		  ed.commit();
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		/*TODO: updating in future versions*/
	}
	
	public void add(final String _address, final String _text, final long date) {
		if (needToClean()) {
			performCleanDataBase();
		}
		Log.i("SMS_SPAM", "ADDED");
		String address = Util.escapeApostrophes(_address);
		String text = Util.escapeApostrophes(_text);
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(ADDRESS_COLUMN, address);
		cv.put(MESSAGE_COLUMN, text);
		cv.put(DATE_COLUMN, date);
		db.insert(TABLE_NAME, null, cv);
		db.close();
	}
	
	public List<Sms> getAsList() {
		if (needToClean()) {
			performCleanDataBase();
		}
		List<Sms> list = new ArrayList<Sms>();
    	SQLiteDatabase db = this.getReadableDatabase();
    	Cursor c = db.query(TABLE_NAME, null, null, null, null, null, null);
    	if (c.moveToNext()) {
    		int dateColIndex = c.getColumnIndex(DATE_COLUMN);
			int textColIndex = c.getColumnIndex(MESSAGE_COLUMN);
			int addressColIndex = c.getColumnIndex(ADDRESS_COLUMN);
			int idColIndex = c.getColumnIndex(ID_COLUMN);
			do {
				String address = c.getString(addressColIndex);
				String text = c.getString(textColIndex);
				int id = c.getInt(idColIndex);
				long date = c.getLong(dateColIndex);
				Sms sms = new Sms(address, text, date);
				sms.setId(id);
				list.add(sms);
		    } while (c.moveToNext());
    	}
    	db.close();
		return list;
	}
	
	/*check is open*/
	public void deleteById(final SQLiteDatabase db, final int id) {
		db.delete(TABLE_NAME, "id" + "='" + id + "'", null);
	}
	
	public void performCleanDataBase() {
		Date curDate = new Date();
		long curDateInMillis = curDate.getTime();
    	SQLiteDatabase db = this.getWritableDatabase();
		Cursor c = db.query(TABLE_NAME, null, null, null, null, null, null);
		if (c.moveToFirst()) {
			int dateColIndex = c.getColumnIndex(DATE_COLUMN);
			int idColIndex = c.getColumnIndex("id");
			do {
				long date = c.getLong(dateColIndex);
				if (date + TIME_TO_LIVE <= curDateInMillis) {
					int id = c.getInt(idColIndex);
					deleteById(db, id);
				}
		    } while (c.moveToNext());
		}
		c.close();
		db.close();
		SharedPreferences sPref = context.getSharedPreferences("preferences", context.MODE_WORLD_READABLE);
		Editor ed = sPref.edit();
		ed.putLong(LAST_CLEAN_PERFORMED_TIME, curDateInMillis);
		ed.commit();
	}
	
	private boolean needToClean() {
		SharedPreferences sPref = context.getSharedPreferences("preferences", context.MODE_WORLD_READABLE);
		long curDateInMillis = (new Date()).getTime();
		long lastCleanTime = sPref.getLong(LAST_CLEAN_PERFORMED_TIME, 0);
		return (lastCleanTime + CLEAN_PERIOD <= curDateInMillis);
	}
	
	
	
	
	
	
}
