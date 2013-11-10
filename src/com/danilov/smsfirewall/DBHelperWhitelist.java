package com.danilov.smsfirewall;

import java.util.ArrayList;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DBHelperWhitelist extends SQLiteOpenHelper {
	
	private Context context;
	private static final String TABLE_NAME = "whitelist";

    public DBHelperWhitelist(Context context) {
      super(context, "myDB", null, 1);
      this.context = context;
      SQLiteDatabase db = this.getWritableDatabase();
      if (!Util.isTableExists(db, TABLE_NAME)) {
    	  createTable(db);
      }
      db.close();
    }
    
    
	@Override
	public void onCreate(SQLiteDatabase db) {
		createTable(db);
	}
	

    private void createTable(final SQLiteDatabase db) {
    	Log.d("g", "--- onCreate database ---");

        db.execSQL("create table " + TABLE_NAME + " ("
		            + "id integer primary key autoincrement," 
		            + "name text," +
		            "number text unique ON CONFLICT FAIL);");
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	
	
	public ArrayList<String> getAsNamesList() {
		ArrayList<String> list = new ArrayList<String>();
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor c = db.query(TABLE_NAME, null, null, null, null, null, null);
		if (c.moveToFirst()) {
			Integer idColIndex = c.getColumnIndex("id");
			int nameColIndex = c.getColumnIndex("name");
			do {
		        list.add(c.getString(nameColIndex));
		    } while (c.moveToNext());
		}
		db.close();
		return list;
	}
	
	public ArrayList<String> getAsNumbersList() {
		ArrayList<String> list = new ArrayList<String>();
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor c = db.query(TABLE_NAME, null, null, null, null, null, null);
		if (c.moveToFirst()) {
			int numberColIndex = c.getColumnIndex("number");
			do {
		        list.add(c.getString(numberColIndex));
		    } while (c.moveToNext());
		}
		db.close();
		return list;
	}
	
	public void deleteFromDb(final String number){
    	SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_NAME, "name" + "='" + number + "'", null);
		db.close();
    }
	
	public boolean contains(final String number) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, null, "number='" + number + "'", null, null, null, null);
		if (!cursor.moveToFirst()) {
		    cursor.close();
		    db.close();
	        return false;
	    }
	    int count = cursor.getCount();
	    return count > 0;
	}
	
	public void addToDb(String name, String number){
	    	ArrayList<String> list = new ArrayList<String>();
	    	SQLiteDatabase db = this.getWritableDatabase();
			ContentValues cv = new ContentValues();
			if (name.equals("")) {
				cv.put("name", number);
			} else {
				cv.put("name", name);
			}
			cv.put("number", number);
			boolean isExistInDb = false;
			boolean isEmpty = false;
			if(number.equals("")){
				isEmpty = true;
			}
			Resources res = context.getResources();
			String exists = res.getString(R.string.alreadyExisits);
			String empty = res.getString(R.string.empty);
			if(!isEmpty){
				try {
					long rowID = db.insertOrThrow(TABLE_NAME, null, cv);
				} catch (SQLiteConstraintException e) {
					isExistInDb = true;
				}
			} else if(isEmpty){
				Toast toast = Toast.makeText(context, empty, Toast.LENGTH_SHORT);
				toast.show();
			}
			if (isExistInDb){
				Toast toast = Toast.makeText(context, exists, Toast.LENGTH_SHORT);
				toast.show();
			}
			db.close();
	    }

}
