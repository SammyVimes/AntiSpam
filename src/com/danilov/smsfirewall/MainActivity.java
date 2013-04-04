package com.danilov.smsfirewall;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends SherlockFragmentActivity {
	
	ArrayList<String> messages = new ArrayList<String>();
	ArrayList<String> sendersNumbersOnly = new ArrayList<String>();
	ArrayList<String> senders = new ArrayList<String>();
	ArrayList<String> smsDates = new ArrayList<String>();
	MyReceiver receiver;
	MyDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		filter.setPriority(-1000); 	//registering receiver, that will invoke update list method
		receiver = new MyReceiver();
		registerReceiver(receiver, filter);
		update();
	}
	
	@SuppressWarnings("unchecked")
	@SuppressLint({ "UseValueOf", "SimpleDateFormat" })
	public void getMessages(){
		messages.clear();
		senders.clear();
		smsDates.clear();
		Uri uriSms = Uri.parse("content://sms/inbox");
		String[] queryProjection = {"address", "body", "person", "date"};
		ArrayList<String> contactIds = new ArrayList<String>();
		Cursor c = this.getContentResolver().query(uriSms, queryProjection, null, null, null);
		int i = 0;
		if (c.moveToFirst()) {
			int bodyColIndex = c.getColumnIndex("body");
			int sendersColIndex = c.getColumnIndex("address");
			int contactIdsColIndex = c.getColumnIndex("person");
			int dateColIndex = c.getColumnIndex("date");
			do {
		        messages.add(c.getString(bodyColIndex));
		        senders.add(c.getString(sendersColIndex));
		        contactIds.add(c.getString(contactIdsColIndex));
		        Date date = new Date(new Long(c.getString(dateColIndex)));
		        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd MMMMMMMMM");
		        String l = sdf.format(date);
		        smsDates.add(l);
		        i++;
		    } while (c.moveToNext() && i < 30);
		}
		c.close();
		sendersNumbersOnly = (ArrayList<String>) senders.clone();
		getContactNames(contactIds);
	}
	
	public void getContactNames(ArrayList<String> contactIds){
		Cursor c = getBaseContext().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		if (c.moveToFirst()) {
			int idColIndex = c.getColumnIndex(ContactsContract.Contacts._ID);
			int nameColIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
			do {
				String tmp = c.getString(idColIndex);
				for(int i = 0; i < contactIds.size(); i++){
					if(contactIds.get(i) != null){
						if(contactIds.get(i).equals(tmp)){
							String name = c.getString(nameColIndex);
							senders.set(i, name);
						}
					}
				}
			} while (c.moveToNext());
		}
		c.close();
		
	}
	
	public class MyReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			update();
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		update();
	}
	
	@Override
	public void onDestroy(){
		unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	@Override
	public void onSaveInstanceState(Bundle saved){
		if(dialog != null){
			dialog.dismiss();
		}
		super.onSaveInstanceState(saved);
	}
	
	public void update(){
		ListView listView = (ListView)findViewById(R.id.listView);
		getMessages();
		MessagesAdapter adapter = new MessagesAdapter(this, senders, messages, smsDates);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new ListListener(adapter));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
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
			case R.id.menu_blacklist:
				intent = new Intent(this, BlackListActivity.class);
				this.startActivity(intent);
				break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private class ListListener implements OnItemClickListener {
		
		MessagesAdapter adapter;
		
		public ListListener(MessagesAdapter adapter){
			super();
			this.adapter = adapter;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			String str = adapter.getItem(position);
			dialog = new MyDialog(str, sendersNumbersOnly.get(position));
			dialog.show(getSupportFragmentManager(), "dlg");
		}
	}
	
	public class MyDialog extends DialogFragment{
		
		private String str;
		private String number;
		
		public MyDialog(String str, String number){
			super();
			this.str = str;
			this.number = number;
		}
		
		
		public Dialog onCreateDialog(Bundle savedInstanceState) {
		    AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
		        .setNeutralButton(R.string.add, new DialogListener())
		        .setMessage(getResources().getString(R.string.add)+ " " + str + " " + getResources().getString(R.string.toBlackList));
		    return adb.create();
		 }
		
		public class DialogListener implements OnClickListener, android.content.DialogInterface.OnClickListener{

			@Override
			public void onClick(DialogInterface dialog, int which) {
				new DBHelper(getBaseContext()).addToDb(number);
			}

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}

			
		}
	}

}
