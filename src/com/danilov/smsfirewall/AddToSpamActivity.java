package com.danilov.smsfirewall;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.danilov.smsfirewall.R;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AddToSpamActivity extends SherlockActivity implements OnClickListener {
	
	private String sender;
	private String message;
	private String messageUntouched;
	private String date;
	private String subStringedMessage;
	private boolean expandedMessage = false;
	
	private String expandString;
	private String minimizeString;
	
	private TextView messageView;
	private Button expand;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_to_spam);
		showUI();
	}
	
	public void showUI(){
		Intent intent = getIntent();
		message = intent.getStringExtra(SMSReceiver.MESSAGE);
		messageUntouched = message;
		sender = intent.getStringExtra(SMSReceiver.SENDER);
		date = intent.getStringExtra(SMSReceiver.DATE);
		expand = (Button) findViewById(R.id.expand);
		expand.setOnClickListener(this);
		messageView = (TextView)findViewById(R.id.messageView);
		Resources res = getResources();
		TextView senderView = (TextView)findViewById(R.id.senderView);
		TextView textView = (TextView)findViewById(R.id.textView);
		String senderTextPart = res.getString(R.string.sender);
		expandString = res.getString(R.string.expand);
		minimizeString = res.getString(R.string.minimize);
		String messageTextPart = res.getString(R.string.messageText);
		int end = 0;
		boolean longer = false;
		if(message.length() > 20){
			end = 20;
			longer = true;
		}else{
			end = message.length();
			expand.setVisibility(View.GONE);
		}
		subStringedMessage = message.substring(0, end);
		if (longer) {
			subStringedMessage = messageTextPart + " " + subStringedMessage + "...";
		} else {
			subStringedMessage = messageTextPart + " " + subStringedMessage;
		}
		message = messageTextPart + " " + message;
		messageView.setText(subStringedMessage);
		senderView.setText(senderTextPart + " " + sender);
		String add = getResources().getString(R.string.add);
		String toBlackList = getResources().getString(R.string.toBlackList);
		textView.setText(add + " " + sender + " " + toBlackList);
		Button yesButton = (Button)findViewById(R.id.yesButton);
		Button noButton = (Button)findViewById(R.id.noButton);
		SharedPreferences sPref = this.getSharedPreferences("preferences", Context.MODE_WORLD_READABLE);
		boolean rated = sPref.getBoolean("rated", false);
		Button settingsButton = (Button)findViewById(R.id.rateButton);
		if (rated) {
			settingsButton.setVisibility(View.GONE);
		}
		yesButton.setOnClickListener(this);
		noButton.setOnClickListener(this);
		settingsButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		switch(arg0.getId()){
			case R.id.yesButton:
				addToBlackList();
				DBSpamCacheHelper helper = new DBSpamCacheHelper(this);
				helper.add(sender, messageUntouched, Long.valueOf(date));
				helper.close();
				finish();
				break;
			case R.id.noButton:
				putMessageToInbox();
				finish();
				break;
			case R.id.rateButton:
				SharedPreferences sPref = this.getSharedPreferences("preferences", Context.MODE_WORLD_READABLE);
				Editor ed = sPref.edit();
				ed.putBoolean("rated", true);
				ed.commit();
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse("market://details?id=com.danilov.smsfirewall"));
				try {
					this.startActivity(i);
				} catch (Exception e) {
					Util.toaster(this, "Market is not installed");
					e.printStackTrace();
				}
				break;
			case R.id.expand:
				if (expandedMessage) {
					close();
				} else {
					expand();
				}
				break;
		}
	}
	
	private void expand() {
		messageView.setText(message);
		expand.setText(minimizeString);
		expandedMessage = true;
	}
	
	private void close() {
		messageView.setText(subStringedMessage);
		expand.setText(expandString);
		expandedMessage = false;
	}
	
	
	
	private void putMessageToInbox(){
		ContentValues values = new ContentValues();
		values.put("address", sender);
		values.put("body", messageUntouched);
		values.put("date", date);
		getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
	}

	private void addToBlackList(){
		DBHelper dbHelper = new DBHelper(this);
		dbHelper.addToDb(sender, sender);
		Util.toaster(this, "Adding to black list");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.add_to_spam_menu, menu);
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
	
}
