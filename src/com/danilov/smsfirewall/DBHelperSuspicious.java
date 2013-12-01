package com.danilov.smsfirewall;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

class DBHelperSuspicious extends SQLiteOpenHelper {
	
	private static String DBVERSION = "0";
	private static String VERSION = "VERSION_SUSPICIOUS";

	Context context;
	
    public DBHelperSuspicious(Context context) {

      super(context, "suspiciousDB", null, 1);
      this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      Log.d("g", "--- onCreate Suspicious database ---");

      db.execSQL("create table mytable ("
          + "id integer primary key autoincrement," 
          + "word text);");
      
      SharedPreferences sPref = context.getSharedPreferences("preferences", context.MODE_WORLD_READABLE);
	  Editor ed = sPref.edit();
	  ed.putString(VERSION, DBVERSION);
	  ed.commit();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
    
    public ArrayList<String> getList(){
    	ArrayList<String> list = new ArrayList<String>();
    	SQLiteDatabase db = this.getWritableDatabase();
		Cursor c = db.query("mytable", null, null, null, null, null, null);
		if (c.moveToFirst()) {
			int wordColIndex = c.getColumnIndex("word");
			do {
				list.add(c.getString(wordColIndex));
		    } while (c.moveToNext());
		}
		c.close();
		db.close();
		return list;
    }
    
    public void deleteFromDb(final String number){
    	SQLiteDatabase db = this.getWritableDatabase();
		db.delete("mytable", "word" + "='" + number + "'", null);
		db.close();
    }
    
    public void addToDb(String word){
    	ArrayList<String> list = new ArrayList<String>();
    	SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		word = Util.escapeApostrophes(word);
		cv.put("word", word);
		boolean isExistInDb = false;
		Cursor c = db.query("mytable", null, null, null, null, null, null);
		if (c.moveToFirst()) {
			int wordColIndex = c.getColumnIndex("word");
			do {
		        list.add(c.getString(wordColIndex));
		    } while (c.moveToNext());
		}
		c.close();
		boolean isEmpty = false;
		if(word.equals("")){
			isEmpty = true;
		}
		for(int i = 0; i < list.size(); i++){
			if(word.equalsIgnoreCase(list.get(i))){
				isExistInDb = true;
				break;
			}
		}
		Resources res = context.getResources();
		String exists = res.getString(R.string.alreadyExisits);
		String empty = res.getString(R.string.empty);
		if(!isExistInDb && !isEmpty){
			db.insert("mytable", null, cv);
		}else if(isEmpty){
			Toast toast = Toast.makeText(context, empty, Toast.LENGTH_SHORT);
			toast.show();
		}else{
			Toast toast = Toast.makeText(context, exists, Toast.LENGTH_SHORT);
			toast.show();
		}
		db.close();
    }
  }