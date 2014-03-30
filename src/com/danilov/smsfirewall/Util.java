package com.danilov.smsfirewall;

import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.style.ReplacementSpan;
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

	public static MyListPair getNameFromContacts(Context ctx){
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
		MyListPair contacts = new MyListPair(names, phones);
		return contacts;
	}
	
	//rewrite
	public static boolean isInContacts(final Context ctx, String number) {
		if (number == null) {
			return false;
		}
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
	
	public static String getContactName(final String phoneNumber, final Context context) {  
		if (phoneNumber == null) {
			return phoneNumber;
		}
		if (phoneNumber.equals("")) {
			return phoneNumber;
		}
        Uri uri;
        String[] projection;
        Uri phone_lookup = Uri.parse("content://com.android.contacts/phone_lookup");
        projection = new String[] { "display_name" };
        String contactName = phoneNumber;
        String _phoneNumber;
        _phoneNumber = phoneNumber.replace("-", "");
        _phoneNumber = _phoneNumber.replace(" ", "");
        String alternatePhoneNumber = phoneNumber;
        if (phoneNumber.contains("+7")) {
        	alternatePhoneNumber = alternatePhoneNumber.replace("+7", "8");
        } else {
        	alternatePhoneNumber = alternatePhoneNumber.replace("8", "+7");
        }
        uri = Uri.withAppendedPath(phone_lookup, Uri.encode(_phoneNumber)); 
        Cursor cursor = null;
        try {
        	cursor = context.getContentResolver().query(uri, projection, null, null, null); 
        } catch (Exception exception) {
        	return contactName;
        }
        String tmp = null;
        if (cursor.moveToFirst()) { 
        	tmp = cursor.getString(0);
        }
        cursor.close();
        if (tmp != null) {
        	return tmp;
        }
        uri = Uri.withAppendedPath(phone_lookup, Uri.encode(_phoneNumber));
        try {
        	cursor = context.getContentResolver().query(uri, projection, null, null, null); 
        } catch (Exception exception) {
        	return contactName;
        }
        if (cursor.moveToFirst()) { 
        	tmp = cursor.getString(0);
        }
        cursor.close();
        if (tmp != null) {
        	return tmp;
        }
        cursor = null;
        return contactName; 
    }
	
	public static String findNameInList(String number, MyListPair contacts){
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
	
	public static boolean phoneNubmersEqual(final String phoneNumber1, final String phoneNumber2) {
		if (phoneNumber1 == null || phoneNumber2 == null) {
			return false;
		}
		String num1 = replaceCharsForNumbers(phoneNumber1);
		String num2 = replaceCharsForNumbers(phoneNumber2);
		return num1.equals(num2);
	}
	
	public static boolean phoneNubmersMatch(final String phoneNumber1, final String phoneNumber2) {
		if (phoneNumber1 == null || phoneNumber2 == null) {
			return false;
		}
		String num1 = replaceCharsForNumbers(phoneNumber1);
		String num2 = replaceCharsForNumbers(phoneNumber2);
		boolean res = num1.contains(num2);
		if (!res) {
			res = num2.contains(num1);
		}
		return res;
	}
	
	private static String replaceCharsForNumbers(final String number) {
		String num = number;
		num = number.toLowerCase(Locale.getDefault());
		num = number.replace(" ", "");
		num = number.replace("-", "");
		num = number.replace("+7", "8");
		return num;
	}
	
	public static String escapeApostrophes(final String string) {
		return string == null ? null : string.replace("'", "''");
	}
	
	public static void Log(final String message) {
		System.out.println(message);
	}
	
}
