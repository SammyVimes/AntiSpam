package com.danilov.smsfirewall;

import java.util.ArrayList;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

class DBHelper extends SQLiteOpenHelper {

	Context context;
	
    public DBHelper(Context context) {
      // ����������� �����������
      super(context, "myDB", null, 1);
      this.context = context;
      if(isDatabaseExist()){
      	if(isUpgradeable()){
      		SQLiteDatabase db = this.getWritableDatabase();	
      		onUpgrade(db, 0, 1);
      	}
      }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      Log.d("g", "--- onCreate database ---");
      // ������� ������� � ������
      db.execSQL("create table mytable ("
          + "id integer primary key autoincrement," 
          + "name text," +
          "nubmer text" +
          "version integer);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	ArrayList<String> numbers = new ArrayList<String>();
	Cursor c = db.query("mytable", null, null, null, null, null, null);
	if (c.moveToFirst()) {
		int nameColIndex = c.getColumnIndex("name");
		do {
		       numbers.add(c.getString(nameColIndex));
		}while (c.moveToNext());
	}
	db.close();
	context.deleteDatabase("myDB");
	ContentValues cv = new ContentValues();
	SQLiteDatabase db = this.getWritableDatabase();
	for(int i = 0; i < numbers.size(); i++){
		cv.clear();
		cv.put("number", numbers.get(i));
		//TODO: HERE FIND NAMES FROM CONTACTS
		db.insert("mytable", null, cv);
	}
	
    }
    
    public void isDatabaseExist(){
    	File dbFile=context.getDatabasePath(myDB);
    	return dbFile.exists();
    }
    
    public void isUpgradeable(){
 
    	int curVersion;
    	int version;
    	SQLiteDatabase db = this.getWritableDatabase();
    	Cursor c = db.query("mytable", null, null, null, null, null, null);
	if (c.moveToFirst()) {
		int versionColIndex = c.getColumnIndex("version");
		//TODO: CHECK THIS IN DEBUGGER!
		do {
		       version = c.getInt(versionColIndex);
		}while (c.moveToNext());
	}
	if(version == null){
		return true;
	}
	return false;
    }
    
    public void addToDb(String name){
    	ArrayList<String> list = new ArrayList<String>();
    	SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("name", name);
		boolean isExistInDb = false;
		Cursor c = db.query("mytable", null, null, null, null, null, null);
		if (c.moveToFirst()) {
			int nameColIndex = c.getColumnIndex("name");
			do {
		        list.add(c.getString(nameColIndex));
		    } while (c.moveToNext());
		}
		c.close();
		boolean isEmpty = false;
		if(name.equals("")){
			isEmpty = true;
		}
		for(int i = 0; i < list.size(); i++){
			if(name.toLowerCase(Locale.getDefault()).equals(list.get(i).toLowerCase(Locale.getDefault()))){
				isExistInDb = true;
				break;
			}
		}
		if(!isExistInDb && !isEmpty){
			long rowID = db.insert("mytable", null, cv);
		}else if(isEmpty){
			Toast toast = Toast.makeText(context, "Empty!", Toast.LENGTH_SHORT);
			toast.show();
		}else{
			Toast toast = Toast.makeText(context, "Already exists!", Toast.LENGTH_SHORT);
			toast.show();
		}
		db.close();
    }
  }
