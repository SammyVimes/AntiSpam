package com.danilov.smsfirewall;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.style.UpdateAppearance;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity {
	
	ArrayList<String> messages = new ArrayList<String>();
	ArrayList<String> sendersNumbersOnly = new ArrayList<String>();
	ArrayList<String> senders = new ArrayList<String>();
	ArrayList<String> smsDates = new ArrayList<String>();
	MessagesAdapter adapter;
	MyReceiver receiver;
	MyDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ProgressBar pb = (ProgressBar)findViewById(R.id.progressBar);
		pb.setVisibility(View.GONE);
		IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		filter.setPriority(-1000); 	//registering receiver, that will invoke update list method
		receiver = new MyReceiver();
		registerReceiver(receiver, filter);
		update();
	}
	

	@SuppressWarnings("unchecked")
	@SuppressLint({ "UseValueOf", "SimpleDateFormat" })
	public int getMessages(int quantity, boolean fromBeginning){
		if(fromBeginning){
			messages.clear();
			senders.clear();
			smsDates.clear();
		}
		int size = messages.size();
		ArrayList<String> messagesTmp = new ArrayList<String>();
		ArrayList<String> sendersTmp = new ArrayList<String>();
		ArrayList<String> smsDatesTmp = new ArrayList<String>();
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
				messagesTmp.add(c.getString(bodyColIndex));
				sendersTmp.add(c.getString(sendersColIndex));
		        contactIds.add(c.getString(contactIdsColIndex));
		        Date date = new Date(new Long(c.getString(dateColIndex)));
		        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd MMMMMMMMM");
		        String l = sdf.format(date);
		        smsDatesTmp.add(l);
		        i++;
		    } while (c.moveToNext() && i < quantity);
		}
		c.close();
		int updatedPart = 0;
		int tmpSize = messagesTmp.size();
		updatedPart = tmpSize - size;
		for(int j = 0; j < tmpSize - size; j++){
			senders.add(sendersTmp.get(j + size));
			messages.add(messagesTmp.get(j + size));
			smsDates.add(smsDatesTmp.get(j + size));
		}
		sendersNumbersOnly = (ArrayList<String>) senders.clone();
		startNumberResolvingTask();
		return updatedPart;
	}
	
	public void startNumberResolvingTask(){
		(new AsyncTask<String, Integer, String>(){

			@Override
			protected String doInBackground(String... params) {
				getContactNames();
				return null;
			}
			
			@Override
			protected void onPostExecute(String result){
				 adapter.notifyDataSetChanged();
			}
			
			
		}).execute();
	}
	
	public void getContactNames(){
		ArrayList<String> tmp = new ArrayList<String>();
		ArrayList<String> names = new ArrayList<String>();
		myListPair listOfNames = BlackListActivity.getNameFromContacts(getBaseContext());
		for(int i = 0; i < senders.size(); i++){
			boolean flag = false;
			String sender = senders.get(i);
			for(int j = 0; j < tmp.size(); j++){
				if(sender.equals(tmp.get(j))){
					flag = true;
					break;
				}
			}
			if(!flag){
				tmp.add(sender);
				names.add(BlackListActivity.findNameInList(sender, listOfNames));
			}
		}
		for(int i = 0; i < senders.size(); i++){
			for(int j = 0; j < tmp.size(); j++){
				if(senders.get(i).equals(tmp.get(j))){
					senders.set(i, names.get(j));
					break;
				}
			}
		}
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
		super.onSaveInstanceState(saved);
	}
	
	public void update(){
		ListView listView = (ListView)findViewById(R.id.listView);
		getMessages(30, true);
		adapter = new MessagesAdapter(this, senders, messages, smsDates);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new ListListener(adapter));
		listView.setOnScrollListener(new EndlessScrollListener());
	}
	
	public int updateOnScrolling(int quantity){
		return getMessages(quantity, false);
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
			dialog = new MyDialog();
			dialog.setMessage(str);
			dialog.setNumber(sendersNumbersOnly.get(position));
			dialog.show(getSupportFragmentManager(), "dlg");
		}
	}
	
	public static class MyDialog extends DialogFragment{
		
		private static String NUMBER_KEY = "NUMBER";
		private static String MESSAGE_KEY = "MESSAGE";
		
		
		private String message;
		private String number;
		private View view;
		private MainActivity activity;
		private CheckBox checkBox;
		
		public MyDialog(){
			super();
		}
		
		public void setMessage(String string){
			this.message = string;
		}
		
		public void setNumber(String number){
			this.number = number;
		}
		
		
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			if(savedInstanceState != null){
				restoreSavedInstanceState(savedInstanceState);
			}
		    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
		        .setNeutralButton(R.string.add, new DialogListener())
		        .setMessage(getResources().getString(R.string.add)+ " " + message + " " + getResources().getString(R.string.toBlackList));
		    activity = (MainActivity) getActivity();
		    LayoutInflater inflater = activity.getLayoutInflater();
		    View view = inflater.inflate(R.layout.dialog_add_to_blacklist, null);
		    builder.setView(view);
		    this.view = view;
		    checkBox = (CheckBox)view.findViewById(R.id.checkBox);
		    checkBox.setChecked(true);
		    return builder.create();
		 }
		
		private void restoreSavedInstanceState(Bundle savedInstanceState){
			message = savedInstanceState.getString(MESSAGE_KEY);
			number = savedInstanceState.getString(NUMBER_KEY);
		}
		
		
		@Override
		public void onSaveInstanceState(Bundle saved){
			saved.putString(NUMBER_KEY, number);
			saved.putString(MESSAGE_KEY, message);
			super.onSaveInstanceState(saved);
		}
		
		public void deleteSmsThread(String num){
			final String number = new String(num);
			(new AsyncTask<String, Integer, String>(){

				@Override
				protected String doInBackground(String... params) {
					Uri uriSms = Uri.parse("content://sms/inbox");
					Cursor c = MyApplication.getAppContext().getContentResolver().query(uriSms, null,null,null,null); 
					if (c.moveToFirst()) {
						int numberColIndex = c.getColumnIndex("address");
						int threadColIndex = c.getColumnIndex("thread_id");
						Integer necessaryThreadId = (Integer) null;
						do {
							String tmpNumber = c.getString(numberColIndex);
					        if(tmpNumber.equals(number)){
					        	necessaryThreadId = c.getInt(threadColIndex);
					        	break;
					        }
					    } while (c.moveToNext());
						if(necessaryThreadId != null){
							MyApplication.getAppContext().getContentResolver().delete(Uri.parse("content://sms/conversations/" + necessaryThreadId),null,null);
						}
					}
					return null;
				}
				
				@Override
				protected void onPostExecute(String result){
					activity.update();
				}
				
				
			}).execute();
		}
		
		
		public class DialogListener implements OnClickListener, android.content.DialogInterface.OnClickListener{

			@Override
			public void onClick(DialogInterface dialog, int which) {
				new DBHelper(MyApplication.getAppContext()).addToDb(message, number);
				if(checkBox.isChecked()){
					deleteSmsThread(number);
				}
			}

			@Override
			public void onClick(View v) {
			}

			
		}
	}
	
	private class EndlessScrollListener implements OnScrollListener{
		
		private int curTotal = 0;
		private int updSize = 0;

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			if(curTotal == 0){
				curTotal = totalItemCount;
			}
			if(firstVisibleItem + 5 > curTotal){
				curTotal += 10; 
				(new AsyncTask<Integer, Integer, String>(){
					
					@Override
					protected void onPreExecute(){
						ProgressBar pb = (ProgressBar)findViewById(R.id.progressBar);
						pb.setVisibility(View.VISIBLE);
					}
					
					@Override
					protected String doInBackground(Integer... params) {
						updSize = updateOnScrolling(params[0]);
						return null;
					}
					
					@Override
					protected void onPostExecute(String result){
						ProgressBar pb = (ProgressBar)findViewById(R.id.progressBar);
						pb.setVisibility(View.GONE);
						adapter.setAll(senders, messages, smsDates, updSize);
					}
				}).execute(totalItemCount + 10, null, null);
			}

		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
		}
		
	}

}
