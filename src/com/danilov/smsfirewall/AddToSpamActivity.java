package com.danilov.smsfirewall;

import com.actionbarsherlock.app.SherlockActivity;
import com.danilov.smsfirewall.R;

import android.content.ContentValues;
import android.content.Intent;
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
	private String date;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_to_spam);
		showUI();
	}
	
	public void showUI(){
		Intent intent = getIntent();
		message = intent.getStringExtra(SMSReceiver.MESSAGE);
		sender = intent.getStringExtra(SMSReceiver.SENDER);
		date = intent.getStringExtra(SMSReceiver.DATE);
		TextView messageView = (TextView)findViewById(R.id.messageView);
		TextView senderView = (TextView)findViewById(R.id.senderView);
		TextView textView = (TextView)findViewById(R.id.textView);
		String senderTextPart = getResources().getString(R.string.sender);
		String messageTextPart = getResources().getString(R.string.messageText);
		int end = 0;
		if(message.length() > 20){
			end = 20;
		}else{
			end = message.length();
		}
		String messageSubString = message.substring(0, end);
		messageView.setText(messageTextPart + " " + messageSubString + "...");
		senderView.setText(senderTextPart + " " + sender);
		String add = getResources().getString(R.string.add);
		String toBlackList = getResources().getString(R.string.toBlackList);
		textView.setText(add + " " + sender + " " + toBlackList);
		Button yesButton = (Button)findViewById(R.id.yesButton);
		Button noButton = (Button)findViewById(R.id.noButton);
		Button settingsButton = (Button)findViewById(R.id.settingsButton);
		yesButton.setOnClickListener(this);
		noButton.setOnClickListener(this);
		settingsButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		switch(arg0.getId()){
			case R.id.yesButton:
				addToBlackList();
				finish();
				break;
			case R.id.noButton:
				putMessageToInbox();
				finish();
				break;
			case R.id.settingsButton:
				Intent intent = new Intent(this, SettingsActivity.class);
				this.startActivity(intent);
				break;
		}
	}
	
	private void putMessageToInbox(){
		ContentValues values = new ContentValues();
		values.put("address", sender);
		values.put("body", message);
		values.put("date", date);
		getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
	}

	private void addToBlackList(){
		DBHelper dbHelper = new DBHelper(this);
		dbHelper.addToDb(sender);
		Toast toast = Toast.makeText(this, "Adding to black list", Toast.LENGTH_SHORT);
		toast.show();
	}
}
