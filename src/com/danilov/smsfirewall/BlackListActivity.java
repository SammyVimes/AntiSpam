package com.danilov.smsfirewall;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import a_vcard.android.syncml.pim.PropertyNode;
import a_vcard.android.syncml.pim.VDataBuilder;
import a_vcard.android.syncml.pim.VNode;
import a_vcard.android.syncml.pim.vcard.VCardException;
import a_vcard.android.syncml.pim.vcard.VCardParser;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class BlackListActivity extends SherlockFragmentActivity implements OnClickListener {
	
	private DBHelper dbHelper;
	private EditText editText;
	private ListView listView;
	public MyDialog dialog;
	private ArrayAdapter<String> adapter;
	private List<String> list = new LinkedList<String>();
	private List<String> idList = new LinkedList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blacklist);
		editText = (EditText) findViewById(R.id.editText);
		Button addButton = (Button) findViewById(R.id.addButton);
		Button fromContactsButton = (Button) findViewById(R.id.fromContactsButton);
		listView = (ListView)findViewById(R.id.listView);
		addButton.setOnClickListener(this);
		fromContactsButton.setOnClickListener(this);
		dbHelper = new DBHelper(this);
		listView.setOnItemClickListener(new ListListener());
		updateList();
		Intent intent = getIntent();
		if(intent != null){
			if("android.intent.action.SEND".equals(intent.getAction())){
				getContact(intent);
			}
		}
	}
	
	public void getContact(Intent intent){
		Uri uri = (Uri)intent.getParcelableExtra(Intent.EXTRA_STREAM);
		ContentResolver cr = getContentResolver();
		InputStream stream = null;
		try {
		    stream = cr.openInputStream(uri);
		} catch (FileNotFoundException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}

		StringBuffer fileContent = new StringBuffer("");
		int ch;
		try {
		    while( (ch = stream.read()) != -1)
		      fileContent.append((char)ch);
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		String data = new String(fileContent);
		VCardParser parser = new VCardParser();
		VDataBuilder builder = new VDataBuilder();
		try {
			boolean parsed = parser.parse(data, "UTF-8", builder);
		} catch (VCardException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        List<VNode> pimContacts = builder.vNodeList;
        String tel = new String();
        for (VNode contact : pimContacts) {
            ArrayList<PropertyNode> props = contact.propList;
            String name = null;
            for (PropertyNode prop : props) {
                if ("FN".equals(prop.propName)) {
                    name = prop.propValue;
                }
                if("TEL".equals(prop.propName)){
                	tel = prop.propValue;
                    break;
                }
            }
            dbHelper.addToDb(name, tel);
            System.out.println("Found contact: " + name);
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.activity_blacklist, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent();
		switch(item.getItemId()){
			case R.id.menu_settings:
				intent = new Intent(this, SettingsActivity.class);
				this.startActivity(intent);
				break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		updateList();
	}

	@Override
	public void onClick(View arg0) {
		switch(arg0.getId()) {
		case R.id.addButton:
			onAddButtonClicked();
			break;
		case R.id.fromContactsButton:
			onFromContactsClicked();
			break;
		}
	}
	
	static final int PICK_CONTACT = 1;
	
	public void onFromContactsClicked() {
		Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(intent, PICK_CONTACT);	
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		 super.onActivityResult(reqCode, resultCode, data);
		 switch (reqCode) {
		 case (PICK_CONTACT) :
		   if (resultCode == SherlockFragmentActivity.RESULT_OK) {
		     Uri contactData = data.getData();
		     Cursor c =  managedQuery(contactData, null, null, null, null);
		     if (c.moveToFirst()) {
		         String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
		         String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
		         String cNumber = null;
	             if (hasPhone.equalsIgnoreCase("1")) {
	        	    Cursor phones = getContentResolver().query( 
	                       ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, 
	                       ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id, 
	                       null, null);
	        	    phones.moveToFirst();
	                cNumber = phones.getString(phones.getColumnIndex("data1"));
	                System.out.println("number is:" + cNumber);
	             } 
		         String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		         if (cNumber != null) {
			         addToBlackList(cNumber, name);
		         }
		     }
		   }
		   break;
		 }
    }
	
	public void onAddButtonClicked() {
		String phone = editText.getText().toString();
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
		String name = Util.getContactName(phone, getApplicationContext());
		addToBlackList(phone, name);
	}
	
	public void addToBlackList(final String phone, final String name) {
		dbHelper.addToDb(name, phone);
		editText.getText().clear();
		editText.setText("");
		updateList();
	}
	
	
	public void updateList(){
		list.clear();
		idList.clear();
		dbHelper.getWritableDatabase();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor c = db.query("mytable", null, null, null, null, null, null);
		
		if (c.moveToFirst()) {
			Integer idColIndex = c.getColumnIndex("id");
			int nameColIndex = c.getColumnIndex("name");
			int nubmerColIndex = c.getColumnIndex("number");
			do {
		        list.add(c.getString(nameColIndex) + " (" + c.getString(nubmerColIndex) + ")");
		        idList.add(c.getString(idColIndex));
		    } while (c.moveToNext());
		}
		Collections.reverse(list);
		Collections.reverse(idList);
		adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, list);
		listView.setAdapter(adapter);
		c.close();
		db.close();
	}
	
	
	@Override
	public void onSaveInstanceState(Bundle saved){
		if(dialog != null){
			dialog.dismiss();
		}
		super.onSaveInstanceState(saved);
	}
	
	private class ListListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			String str = adapter.getItem(position);
			dialog = new MyDialog(position, str);
			dialog.show(getSupportFragmentManager(), "dlg");
		}
	}
	
	@SuppressLint("ValidFragment")
	public class MyDialog extends DialogFragment{
		
		private int id;
		private String str;
		
		public MyDialog(int id, String str){
			super();
			this.id = id;
			this.str = str;
		}
		
		
		public Dialog onCreateDialog(Bundle savedInstanceState) {
		    AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
		        .setNeutralButton(R.string.delete, new DialogListener())
		        .setMessage(getResources().getString(R.string.text)+ " " + str + "?");
		    return adb.create();
		}
		
		public void onDismiss(DialogInterface _dialog) {
		   super.onDismiss(_dialog);
		   dialog = null;
		   updateList();
		 }
		
		public class DialogListener implements OnClickListener, android.content.DialogInterface.OnClickListener{

			@Override
			public void onClick(DialogInterface _dialog, int which) {
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				Log.d("11", idList.get(id));
				db.delete("mytable", "id" + "=" + idList.get(id), null);
				db.close();
				dialog = null;
			}

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}

			
		}
	}


}
