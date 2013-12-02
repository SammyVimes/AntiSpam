package com.danilov.smsfirewall;

import java.util.List;

import Custom.CustomDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.danilov.smsfirewall.adapter.SmsAdapter;

public class SpamStoreActivity extends SherlockFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_spam_store);
		DBSpamCacheHelper helper = new DBSpamCacheHelper(this);
		List<Sms> list = helper.getAsList();
		SmsAdapter adapter = new SmsAdapter(getBaseContext(), list);
		ListView listView = (ListView) findViewById(R.id.listView);
		MyOnItemClickListener listener = new MyOnItemClickListener();
		listView.setOnItemClickListener(listener);
		listView.setCacheColorHint(Color.TRANSPARENT);
		listView.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.spam_store, menu);
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
	
	private void update() {
		DBSpamCacheHelper helper = new DBSpamCacheHelper(this);
		List<Sms> list = helper.getAsList();
		SmsAdapter adapter = new SmsAdapter(getBaseContext(), list);
		ListView listView = (ListView) findViewById(R.id.listView);
		listView.setAdapter(adapter);
		helper.close();
	}
	
	private class MyOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Sms msg = (Sms) parent.getAdapter().getItem(position);
			MyDialog dialog = new MyDialog();
			dialog.setSender(msg.getAddress());
			dialog.setMessage(msg.getText());
			dialog.setDate(Long.valueOf(msg.getDate()).toString());
			dialog.setId(msg.getId());
			dialog.show(getSupportFragmentManager(), "TAG");
		}
		
	}
	
	public static class MyDialog extends DialogFragment {
		
		private int id;
		private String sender;
		private String message;
		private String date;
		
		private static final String ID_STRING = "ID_STRING";
		private static final String SENDER_STRING = "SENDER_STRING";
		private static final String MESSAGE_STRING = "MESSAGE_STRING";
		private static final String DATE_STRING = "DATE_STRING";
		
	
		
		public void setId(int id) {
			this.id = id;
		}


		public void setSender(String sender) {
			this.sender = sender;
		}


		public void setMessage(String message) {
			this.message = message;
		}


		public void setDate(String date) {
			this.date = date;
		}


		public Dialog onCreateDialog(Bundle savedInstanceState) {
			if(savedInstanceState != null){
				restoreSavedInstanceState(savedInstanceState);
			}
			DialogButtonListener listener = new DialogButtonListener();
			CustomDialog.Builder builder = new CustomDialog
					.Builder(getActivity())
			        .setTitle(R.string.restore_message)
					.setPositiveButton(R.string.yes, listener)
					.setNegativeButton(R.string.no, listener);
			return builder.create();
		}
		
		private class DialogButtonListener implements DialogInterface.OnClickListener {


			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					putMessageToInbox();
					DBSpamCacheHelper helper = new DBSpamCacheHelper(getActivity());
					SQLiteDatabase db = helper.getWritableDatabase();
					helper.deleteById(db, id);
					db.close();
					helper.close();
					((SpamStoreActivity)getActivity()).update();
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					dismiss();
					break;
				}
			}
			
		}
		
		private void putMessageToInbox() {
			ContentValues values = new ContentValues();
			values.put("address", sender);
			values.put("body", message);
			values.put("date", date);
			getActivity().getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
		}

		
		private void restoreSavedInstanceState(Bundle savedInstanceState){
			id = savedInstanceState.getInt(ID_STRING);
			sender = savedInstanceState.getString(SENDER_STRING);
			message = savedInstanceState.getString(MESSAGE_STRING);
			date = savedInstanceState.getString(DATE_STRING);
		}
		
		@Override
		public void onSaveInstanceState(Bundle saved){
			saved.putInt(ID_STRING, id);
			saved.putString(SENDER_STRING, sender);
			saved.putString(MESSAGE_STRING, message);
			saved.putString(DATE_STRING, date);
			super.onSaveInstanceState(saved);
		}
		
		
	}

}
