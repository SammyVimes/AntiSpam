package com.danilov.smsfirewall;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;


public class SettingsActivity extends SherlockFragmentActivity {
	
	public static final String BLOCK_CALLS_PARAMETER = "BLOCK_CALLS_PARAMETER";
	public static final String BLOCK_UNKNOWN_PARAMETER = "BLOCK_UNKNOWN_PARAMETER";
	public static final String STORE_SPAM_DAYS = "STORE_SPAM_DAYS";
	
	private CheckBox blockCalls;
	private CheckBox blockUnknown;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		blockCalls = (CheckBox) findViewById(R.id.blockCalls);
		blockUnknown = (CheckBox) findViewById(R.id.blockUnknown);
		Button button = (Button)findViewById(R.id.suspiciousButton);
		Button whiteListButton = (Button)findViewById(R.id.whiteListButton);
		OnClickListener listener = new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				switch (arg0.getId()) {
					case R.id.suspiciousButton:
						Dialog dlg = new Dialog();
						dlg.show(getSupportFragmentManager(), "mainDialog");
						break;
					case R.id.whiteListButton:
						WhiteListDialog wDlg = new WhiteListDialog();
						wDlg.show(getSupportFragmentManager(), "mainDialog");
						break;
				}
			}
			
		};
		button.setOnClickListener(listener);
		whiteListButton.setOnClickListener(listener);
	}
	
	@Override
	protected void onPause(){
		saveSettings();
		super.onPause();
	}
	
	@Override
	protected void onResume(){
		loadSettings();
		super.onResume();
	}
	
	private void saveSettings(){
		SharedPreferences sPref = getSharedPreferences("preferences", MODE_WORLD_READABLE);
		Editor ed = sPref.edit();
		ed.putBoolean(BLOCK_CALLS_PARAMETER, blockCalls.isChecked());
		ed.putBoolean(BLOCK_UNKNOWN_PARAMETER, blockUnknown.isChecked());
		EditText t = (EditText) findViewById(R.id.storeSpamDays);
		int days = Integer.valueOf(t.getText().toString());
		ed.putInt(STORE_SPAM_DAYS, days);
		ed.commit();
	}
	
	private void loadSettings(){
		SharedPreferences sPref = getSharedPreferences("preferences", MODE_WORLD_READABLE);
	    boolean blockUnknowChecked = sPref.getBoolean(BLOCK_UNKNOWN_PARAMETER, false);
	    boolean blockCallsChecked = sPref.getBoolean(BLOCK_CALLS_PARAMETER, false);
		int days = sPref.getInt(STORE_SPAM_DAYS, 1);
		EditText t = (EditText) findViewById(R.id.storeSpamDays);
		t.setText(String.valueOf(days));
	    blockCalls.setChecked(blockCallsChecked);
	    blockUnknown.setChecked(blockUnknowChecked);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.settings_activity, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent();
		switch(item.getItemId()){
			case R.id.rate:
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse("market://details?id=com.danilov.smsfirewall"));
				try {
					this.startActivity(i);
				} catch (Exception e) {
					Util.toaster(this, "Market is not installed");
					e.printStackTrace();
				}
				break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
}
