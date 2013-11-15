package com.danilov.smsfirewall;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.widget.Toast;

public class Util {
	
	static boolean isTableExists(SQLiteDatabase db, String tableName) {
	    if (tableName == null || db == null || !db.isOpen()) {
	        return false;
	    }
	    Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] {"table", tableName});
	    if (!cursor.moveToFirst()) {
	        return false;
	    }
	    int count = cursor.getInt(0);
	    cursor.close();
	    return count > 0;
	}
	

	
	public static void toaster(final Context context, final String msg) {
		Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		toast.show();
	}

	public static myListPair getNameFromContacts(Context ctx){
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<String> phones = new ArrayList<String>();
		Cursor c = ctx.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, null, null, null);
		if (c.moveToFirst()) {
			int phoneColIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
			int phoneTypeColIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
			int nameColIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
			do {
				String contactName = c.getString(nameColIndex);
				String phone = c.getString(phoneColIndex);
				int type = c.getInt(phoneTypeColIndex);
				if(phone != null && type == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE){
					phone = phone.replace("-", "");
					phone = phone.replace(" ", "");
					names.add(contactName);
					phones.add(phone);
				}
			} while (c.moveToNext());
		}
		c.close();
		myListPair contacts = new myListPair(names, phones);
		return contacts;
	}
	
	public static boolean isInContacts(final Context ctx, String number) {
		boolean res = false;
		number = number.replace("-", "");
		number = number.replace(" ", "");
		Cursor c = ctx.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, null, null, null);
		if (c.moveToFirst()) {
			int phoneColIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
			int phoneTypeColIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
			do {
				String phone = c.getString(phoneColIndex);
				int type = c.getInt(phoneTypeColIndex);
				if(phone != null && type == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE){
					phone = phone.replace("-", "");
					phone = phone.replace(" ", "");
					if (number.equals(phone)) {
						return true;
					}
				}
			} while (c.moveToNext());
		}
		c.close();
		return res;
	}
	
	public static String findNameInList(String number, myListPair contacts){
		String name = number;
		ArrayList<String> numbers = contacts.getPhones();
		for(int i = 0; i < numbers.size(); i++){
			if(number.equals(numbers.get(i))){
				name = contacts.getNames().get(i);
				break;
			}
		}
		return name;
	}
	
	public static void Log(final String message) {
		System.out.println(message);
	}
	
}
