package com.danilov.smsfirewall;

import java.io.File;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

class DBHelper extends SQLiteOpenHelper {
	
	private static String DBVERSION = "3";
	private static String VERSION = "VERSION";

	Context context;
	
    public DBHelper(Context context) {

      super(context, "myDB", null, 1);
      this.context = context;
      if(isDatabaseExist()) {
		SQLiteDatabase db = this.getWritableDatabase();	
      	if(isUpgradeable()) {
      		onUpgrade(db, 0, 1);
      	}
      	if (!isTableExists(db)) {
      		createTable(db);
      	}
      	db.close();
      }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	createTable(db);
    }
    
    private void createTable(final SQLiteDatabase db) {
    	Log.d("g", "--- onCreate database ---");

        db.execSQL("create table mytable ("
            + "id integer primary key autoincrement," 
            + "name text," +
            "number text unique ON CONFLICT FAIL);");
        SharedPreferences sPref = context.getSharedPreferences("preferences", context.MODE_WORLD_READABLE);
  		Editor ed = sPref.edit();
  		ed.putString(VERSION, DBVERSION);
  		ed.commit();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	boolean updated = false;
    	if (!isTableExists(db)) {
      		createTable(db);
      		updated = true;
      		return;
      	}
	    ArrayList<String> numbers = new ArrayList<String>();
	    MyListPair listOfNames = Util.getNameFromContacts(context);
		Cursor c = db.query("mytable", null, null, null, null, null, null);
		if (c.moveToFirst()) {
			int nameColIndex = c.getColumnIndex("name");
			do {
			       numbers.add(c.getString(nameColIndex));
			}while (c.moveToNext());
		}
		int a = db.delete("mytable", null, null);
		db.execSQL("DROP TABLE IF EXISTS mytable");
		if (!updated) {
			createTable(db);
		}
		ContentValues cv = new ContentValues();
		db = this.getWritableDatabase();
		for(int i = 0; i < numbers.size(); i++){
			cv.clear();
			cv.put("number", numbers.get(i));
			String name = Util.findNameInList(numbers.get(i), listOfNames);
			cv.put("name", name);
			db.insert("mytable", null, cv);
		}
		SharedPreferences sPref = context.getSharedPreferences("preferences", context.MODE_WORLD_READABLE);
		Editor ed = sPref.edit();
		ed.putString(VERSION, DBVERSION);
		ed.commit();
	
    }
    
    public boolean isDatabaseExist() {
    	File dbFile = context.getDatabasePath("myDB");
    	return dbFile.exists();
    }
    
    public boolean isTableExists(final SQLiteDatabase db) {
    	return Util.isTableExists(db, "mytable");
    }
    
    public boolean isUpgradeable() {
    	String version = "";
    	SharedPreferences sPref = context.getSharedPreferences("preferences", context.MODE_WORLD_READABLE);
		version = sPref.getString(VERSION, "0");
		if(!(version.equals(DBVERSION))){
			return true;
		}
		return false;
    }
    
    public void addToDb(String name, String number){
		Resources res = context.getResources();
		String exists = res.getString(R.string.alreadyExisits);
		String empty = res.getString(R.string.empty);
    	SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		name = Util.escapeApostrophes(name);
		number = Util.escapeApostrophes(number);
		if (name == null) {
			name = number;
		}
		if (number == null) {
			Toast.makeText(context, empty, Toast.LENGTH_SHORT).show();
			return;
		}
		cv.put("name", name);
		cv.put("number", number);
		boolean isExistInDb = false;
		boolean isEmpty = false;
		if(number.equals("")){
			isEmpty = true;
		}
		if(!isEmpty){
			try {
				long rowID = db.insertOrThrow("mytable", null, cv);
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